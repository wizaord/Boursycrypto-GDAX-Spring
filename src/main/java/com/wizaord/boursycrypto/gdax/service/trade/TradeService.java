package com.wizaord.boursycrypto.gdax.service.trade;

import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import com.wizaord.boursycrypto.gdax.domain.E_TradingSellMode;
import com.wizaord.boursycrypto.gdax.domain.api.Fill;
import com.wizaord.boursycrypto.gdax.domain.api.Order;
import com.wizaord.boursycrypto.gdax.domain.feedmessage.*;
import com.wizaord.boursycrypto.gdax.service.AccountService;
import com.wizaord.boursycrypto.gdax.service.gdax.OrderService;
import com.wizaord.boursycrypto.gdax.service.notify.SlackService;
import com.wizaord.boursycrypto.gdax.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.wizaord.boursycrypto.gdax.domain.E_TradingMode.ACHAT;
import static com.wizaord.boursycrypto.gdax.domain.E_TradingMode.VENTE;
import static com.wizaord.boursycrypto.gdax.domain.E_TradingSellMode.BENEFICE;
import static com.wizaord.boursycrypto.gdax.domain.E_TradingSellMode.WAITING_FOR_BENEFICE;
import static com.wizaord.boursycrypto.gdax.utils.MathUtils.df;

@Service
public class TradeService {

    private static final Logger LOG = LoggerFactory.getLogger(TradeService.class);

    @Autowired
    private SlackService slackService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ApplicationProperties appProp;

    private Double lastCurrentPriceReceived;
    private double currentPrice;
    private Fill lastBuyOrder;
    private OrderOpen stopOrderCurrentOrder;
    @Autowired
    private TradingMode tradeMode;

    public void notifyNewTickerMessage(final Ticker ticMessage) {
        this.lastCurrentPriceReceived = ticMessage.getPrice().doubleValue();
        LOG.debug("New Ticker value {}", this.lastCurrentPriceReceived);
    }

    /**
     * Algo mis en place.
     * Si pas encore de cours sur le prix, on ne fait rien
     * Si on est pas en mode VENTE, on ne fait rien
     * Si on est en mode VENTE
     * - on regarde si un stopOrder est positionné. Si non, on le positionne a XX% en dessous du prix en cours (possible si lors du demarrage de l'application, le cours est tellement bas qu'on ne peut pas mettre le stopOrder)
     * - on calcule la balance et on l'affiche
     * - si on est en deficite, on ne change pas le stop order
     * - si on est en bénéfice, on positionne le stopOrder juste pour gagner de l'argent
     * - et ensuite on fait monter ce stopOrder en fonction de la courbe
     */
    @Scheduled(fixedRateString = "${application.trader.delay}")
    public synchronized void doTrading() {
        // si on a pas de cours, on ne fait rien. Sans prix, on ne peut rien faire
        if (this.lastCurrentPriceReceived == null) {
            LOG.info("en attente d une premiere transaction pour connaitre le cours");
            return;
        }

        if (currentPrice == this.lastCurrentPriceReceived) {
            LOG.debug("Pas d'evolution du Prix. En attente de changement.");
            return;
        }

        // on va travailler avec le currentPrice, on le sauvegarde
        this.currentPrice = this.lastCurrentPriceReceived;

        switch (this.tradeMode.getTraderMode()) {
            case ACHAT:
                LOG.info("MODE ACHAT - cours {}", this.currentPrice);
                break;
            case VENTE:
                this.logVenteEvolution();
                if (!this.appProp.getTrader().getModeVisualisation()) {
                    LOG.debug("MODE VENTE");
                    this.doTradingSell();
                }
                break;
        }
    }


    /**
     * Fonction qui détermine le mode de vente. Soit en benefice et on suit la courbe. Soit en mode attente
     * On est en mode benefice et donc on suit la courbe qui monte si :
     * - possible si stopOrder n'existe pas              et benefice supérieur à la valeur configurée dans le fichier de configuration
     * - possible si stopOrder inférieur au prix d'achat et benefice supérieur à la valeur configurée dans le fichier de configuration
     * - possible si le prix du stopOrder est supérieur au prix d'achat => deja en mode benefice
     *
     * @returns {E_TRADESELLMODE}
     */
    private E_TradingSellMode determineTradeSellMode() {
        final double coursRequisPourBenefice = MathUtils
                .calculateAddPourcent(this.lastBuyOrder.getPrice().doubleValue(), appProp.getTrader().getVente().getBenefice()
                        .getPourcentBeforeStartVenteMode());
        final double lastOrderPrice = this.lastBuyOrder.getPrice().doubleValue();
        final boolean isStopOrderPlaced = (this.stopOrderCurrentOrder != null);

        if (isStopOrderPlaced) {
            final double sellOrderPrice = this.stopOrderCurrentOrder.getPrice().doubleValue();
            if (sellOrderPrice > lastOrderPrice) {
                // on est dans le cas où on a déjà été en BENEFICE. On y reste
                return BENEFICE;
            }
        }
        // le stop order est posé ou pas. On est en bénéfice uniquement si le cours le permet
        if (this.currentPrice >= coursRequisPourBenefice) {
            return BENEFICE;
        }
        // on a pas engendré assez de bénéfices
        return WAITING_FOR_BENEFICE;
    }

    public void notifyBuyOrderPassed(Fill order) {
        // on recherche toutes les fills pour cette order.
        Optional<List<Fill>> fills = this.orderService.loadFillsForOrderId(order.getOrder_id());

        if (fills.isPresent()) {
            order.setFee(new BigDecimal(0));
            order.setSize(new BigDecimal(0));
            Fill reducedFill = fills.get().stream()
                    .reduce(order, (a, b) -> {
                        a.setFee(a.getFee().add(b.getFee()));
                        a.setSize(a.getSize().add(b.getSize()));
                        return a;
                    });
            final String message = "NEW FILL - Price <" + df.format(order.getPrice()) + "> size<" + df.format(reducedFill.getSize()) + "> fee<" + df.format(reducedFill.getFee()) + ">";
            LOG.info(message);
            slackService.postCustomMessage(message);
            this.tradeMode.setTraderMode(VENTE);
            this.accountService.refreshBalance();
            this.lastBuyOrder = reducedFill;
        }
    }

    public void notifySellOrderFinished(final Match order) {
        final double sellPrice = order.getPrice();
        final double balance = getBalance(sellPrice, order.getSize());
        final String message = "ORDER PASSED => price: " + df.format(sellPrice) + " quantity :" + df.format(order.getSize()) + " - gain/perte " + df.format(balance)+ " evol: " +df.format(MathUtils.calculatePourcentDifference(sellPrice, this.lastBuyOrder.getPrice().doubleValue()));

        LOG.info(message);
        this.slackService.postCustomMessage(message);

        stopOrderRemove();
        this.accountService.refreshBalance();
        if (this.accountService.getBtc() > 0) {
            //Still BTC... Set in VENTE MODE
            this.tradeMode.setTraderMode(VENTE);
        } else {
            // nothing to sell, pass in BUY MODE
            this.tradeMode.setTraderMode(ACHAT);
        }
    }

    public void logVenteEvolution() {
        final double fee = this.lastBuyOrder.getFee().doubleValue();
        final double price = this.lastBuyOrder.getPrice().doubleValue();
        final double evolution = MathUtils.calculatePourcentDifference(this.currentPrice, this.lastBuyOrder.getPrice().doubleValue());

        String message = "COURS EVOL : - achat " + df.format(price) + " - fee " + df.format(fee) + " - now " + this.currentPrice;
        message += " - benefice " + df.format(this.getBalance(this.currentPrice, this.lastBuyOrder.getSize().doubleValue())) + " E - evolution " + df.format(evolution) + "%";
        message += (this.stopOrderCurrentOrder != null) ? " <sop " + df.format(this.stopOrderCurrentOrder.getPrice()) + "> " : " <sonp>";
        LOG.info(message);
    }


    public double getBalance(final double currentPrice, final double quantity) {
        final double lastOrderPrice = this.lastBuyOrder.getPrice().doubleValue();
        double feeAchat = this.lastBuyOrder.getFee().doubleValue();
        if (feeAchat != 0) {
            feeAchat = quantity * lastOrderPrice * 0.0025;
        }
        final double feeVente = quantity * currentPrice * 0.0025;

        final double prixVente = (quantity * currentPrice) - feeVente;
        final double coutAchat = (quantity * lastOrderPrice) + feeAchat;
        return prixVente - coutAchat;
    }

    /**
     * realisation du trading en mode VENTE
     */
    private void doTradingSell() {
        final boolean isStopOrderPlaced = (this.stopOrderCurrentOrder != null);

        // positionnement du stop order de secours si activé dans le fichier de configuration
        if (this.appProp.getTrader().getVente().getSecureStopOrder().getActivate() && !isStopOrderPlaced) {
            final double negativeWaitPourcent = this.appProp.getTrader().getVente().getSecureStopOrder().getPourcent();
            final double stopPrice = MathUtils.calculateRemovePourcent(this.currentPrice, negativeWaitPourcent);
            LOG.info("MODE VENTE - Place a SECURE stop order to {}", df.format(stopPrice));
            this.stopOrderPlace(stopPrice);
            return;
        }

        // on verifie si on est deja en benefice ou non
        //      - possible si stopOrder n'existe pas              et benefice supérieur à la valeur configurée dans le fichier de configuration
        //      - possible si stopOrder inférieur au prix d'achat et benefice supérieur à la valeur configurée dans le fichier de configuration
        //      - possible si le prix du stopOrder est supérieur au prix d'achat => deja en mode benefice
        E_TradingSellMode sellMode = this.determineTradeSellMode();
        switch (sellMode) {
            case WAITING_FOR_BENEFICE:
                final double coursRequisPourBenefice = MathUtils
                        .calculateAddPourcent(this.lastBuyOrder.getPrice().doubleValue(), this.appProp.getTrader().getVente().getBenefice()
                                .getPourcentBeforeStartVenteMode());
                LOG.debug("MODE VENTE - Not enougth benef. Waiting benefice to : {}", df.format(coursRequisPourBenefice));
                break;
            case BENEFICE:
                LOG.debug("MODE VENTE - Sell Order posts in benefice. Just wait or replace sell order");
                this.doTradingSellBenefice();
                break;
        }
    }

    /**
     * Fonction de gestion quand on est en mode vente et BENEFICE
     * Si le stopOrder n'est pas positionné ou inférieur au prix du lastOrderPrice, on le position au seuil minimal
     * Ensuite on fait monter ce stopOrder en fonction du cours
     */
    private void doTradingSellBenefice() {
        final double lastOrderPrice = this.lastBuyOrder.getPrice().doubleValue();
        final double seuilStopPrice = MathUtils
                .calculateAddPourcent(lastOrderPrice, this.appProp.getTrader().getVente().getBenefice().getInitialPourcent());
        final boolean isStopOrderPlaced = (this.stopOrderCurrentOrder != null);

        // test si aucun stop order n'est positionné
        if (!isStopOrderPlaced) {
            // positionnement d'un stop order au prix seuil
            this.stopOrderPlace(seuilStopPrice);
            return;
        }

        // recuperation du seuil du stopOrder
        final double currentStopOrderPrice = this.stopOrderCurrentOrder.getPrice().doubleValue();

        // test si le stop order est le stop de secours
        if (isStopOrderPlaced && currentStopOrderPrice < lastOrderPrice) {
            this.stopOrderPlace(seuilStopPrice);
            return;
        }

        // on est dans les benefices et on a le stop order deja positionne pour assurer notre argent.
        // on fait donc monter le stop en fonction de la hausse de la courbe
        final double newStopOrderPrice = MathUtils
                .calculateRemovePourcent(this.currentPrice, this.appProp.getTrader().getVente().getBenefice().getFollowingPourcent());

        if (newStopOrderPrice <= currentStopOrderPrice) {
            LOG.debug("Cours en chute, on ne repositionne pas le stopOrder qui est a {}", df.format(currentStopOrderPrice));
        } else {
            this.stopOrderPlace(newStopOrderPrice);
            return;
        }
    }


    void stopOrderRemove() {
        if (this.stopOrderCurrentOrder != null) {
            try {
                this.orderService.cancelOrder(this.stopOrderCurrentOrder.getOrderId());
            } catch (HttpClientErrorException e) {
                LOG.debug("order already deleted");
            }
            this.stopOrderCurrentOrder = null;
        }
    }
    /**
     * Fonction qui positionne un stopOrder a XX% en dessous du court actuel.
     * Le XX% est configurable dans le fichier de configuration
     */
    public void stopOrderPlace(final double price) {
        // si un stop order est deja present, il faut le supprimer
        stopOrderRemove();
        this.orderService.placeStopSellOrder(price, this.accountService.getBtc())
                .ifPresent(order -> notifySellOrderActivated(new OrderActivated(order)));
    }

    /**
     * Notifie le positionnement d'un StopOrder.
     *
     * @param order
     */
    public void notifySellOrderActivated(final OrderActivated order) {
        notifySellOrderOpen(new OrderOpen(order));
    }

    /**
     * Notifie le positionnement d'un ordre de vente
     *
     * @param order
     */
    public void notifySellOrderOpen(final OrderOpen order) {
        slackService.postCustomMessage("SELL ORDER HANDLE a " + order.getPrice() + " pour " + order.getRemainingSize() + " coins");
        this.stopOrderCurrentOrder = order;
        this.tradeMode.setTraderMode(VENTE);
    }

    /**
     * the order identify with the orderId is canceled.
     * This order is removed from the trade service
     *
     * @param orderId
     */
    public void notifySellOrderCanceled(String orderId) {
        if (this.stopOrderCurrentOrder != null && this.stopOrderCurrentOrder.getOrderId().compareTo(orderId) == 0) {
            final String message = "Order with ID " + orderId + " is canceled";
            LOG.info(message);
            this.slackService.postCustomMessage(message);
            this.stopOrderCurrentOrder = null;
        } else {
            LOG.warn("Order with Id {} has not handle by application", orderId);
        }
        //refresh balance
//        this.accountService.refreshBalance();
    }

    /**
     * A buy order has been canceled. Refresh the account and recalculate mode
     * @param orderDoneMessage
     */
    public void notifyBuyOrderCanceled(OrderDone orderDoneMessage) {
        this.determineTradeMode();
    }


    /**
     *
     // if order is placed => sell MODE
     // if BTC exists => sell MODE
     // else => BUY MODE
     */
    public void determineTradeMode() {
        this.accountService.refreshBalance();
        Optional<List<Order>> orders = orderService.loadSellOrders();
        boolean isOrderExixt = (orders.isPresent() && ! orders.get().isEmpty());
        if (this.accountService.getBtc() > 0 || isOrderExixt) {
            // notify order in the trade service
            if (isOrderExixt) {
                final Order firstOrder = orders.get().get(0);
                this.notifySellOrderActivated(new OrderActivated(firstOrder));
            }
            // recuperation et injection de l'ordre d'achat
            final Fill lastFill = this.orderService.getLastBuyFill().get();
            this.notifyBuyOrderPassed(lastFill);
            // mode vente
            this.tradeMode.setTraderMode(VENTE);
        } else {
            // mode achat
            this.tradeMode.setTraderMode(ACHAT);
        }
    }
}
