package com.wizaord.boursycrypto.gdax.service;

import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import com.wizaord.boursycrypto.gdax.domain.E_TradingMode;
import com.wizaord.boursycrypto.gdax.domain.api.Fill;
import com.wizaord.boursycrypto.gdax.domain.api.Order;
import com.wizaord.boursycrypto.gdax.domain.feedmessage.Ticker;
import com.wizaord.boursycrypto.gdax.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Optional;

import static com.wizaord.boursycrypto.gdax.domain.E_TradingMode.ACHAT;
import static com.wizaord.boursycrypto.gdax.domain.E_TradingMode.VENTE;

@Service
public class TradeService {

  private static final Logger LOG = LoggerFactory.getLogger(TradeService.class);

  @Autowired
  private AccountService accountService;
  @Autowired
  private OrderService orderService;
  @Autowired
  private ApplicationProperties appProp;

  private DecimalFormat df = new DecimalFormat("#.##");
  private Double lastCurrentPriceReceived;
  private double currentPrice;
  private E_TradingMode traderMode = E_TradingMode.NOORDER;
  private Order lastBuyOrder;
  private Order stopOrderCurrentOrder;

  public void notifyNewTickerMessage(final Ticker ticMessage) {
    this.lastCurrentPriceReceived = ticMessage.getPrice().doubleValue();
    LOG.info("New Ticker value {}", this.lastCurrentPriceReceived);
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
  @Scheduled(fixedRate = 5000)
  public synchronized void doTrading() {
    // si on a pas de cours, on ne fait rien. Sans prix, on ne peut rien faire
    if (this.lastCurrentPriceReceived == null) {
      LOG.info("en attente d une premiere transaction pour connaitre le cours");
      return;
    }

    // on va travailler avec le currentPrice, on le sauvegarde
    this.currentPrice = this.lastCurrentPriceReceived;

    switch (this.traderMode) {
      case NOORDER:
        LOG.info("MODE UNKNOWN- determination du mode de fonctionnement");
        this.determineTradeMode();
        break;
      case ACHAT:
        LOG.info("MODE ACHAT - cours {]", this.currentPrice);
        //        this.doTradingBuy();
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

  private void determineTradeMode() {
    // on va verifier si on a pas encore des coins.
    if (this.accountService.getBtc() > 0) {
      LOG.info("CHECK MODE - coin in wallet <{}>. Looking for last buy order", this.accountService.getBtc().doubleValue());
      final Optional<Fill> lastBuyFill = this.orderService.getLastBuyFill();
      if (lastBuyFill.isPresent()) {
        LOG.info("CHECK MODE - Find last order buy. Inject order in this AWESOME project");
        this.notifyNewOrder(lastBuyFill.get().mapToOrder());
        return;
      }
    }
    LOG.info("CHECK MODE - No Btc in wallet. Set en ACHAT MODE");
    this.traderMode = ACHAT;
  }

  public void notifyNewOrder(final Order order) {
    LOG.info("NEW ORDER - Receive order {}", order);
    //    SlackService._instance.postMessage('NEW ORDER - Handle order ' + JSON.stringify(order));
    this.accountService.refreshBalance();
    this.lastBuyOrder = order;
    this.traderMode = VENTE;
  }

  public void logVenteEvolution() {
    final double fee = this.lastBuyOrder.getFill_fees().doubleValue();
    final double price = this.lastBuyOrder.getPrice().doubleValue();
    final double evolution = MathUtils.calculatePourcentDifference(this.currentPrice, this.lastBuyOrder.getPrice().doubleValue());

    String message = "COURS EVOL : - achat " + df.format(price) + " - fee " + df.format(fee) + " - now " + this.currentPrice;
    message += " - benefice " + df.format(this.getBalance(this.currentPrice)) + "€ - evolution " + df.format(evolution) + "%";
    LOG.info(message);
  }


  public double getBalance(final double currentPrice) {
    final double lastOrderPrice = this.lastBuyOrder.getPrice().doubleValue();
    final double quantity = this.lastBuyOrder.getSize().doubleValue();
    final double feeAchat = this.lastBuyOrder.getFill_fees().doubleValue();
    final double feeVente = quantity * currentPrice * 0.0025;

    final double prixVente = (quantity * currentPrice) - feeVente;
    final double coutAchat = (quantity * lastOrderPrice) + feeAchat;
    return prixVente - coutAchat;
  }

  /**
   * realisation du trading en mode VENTE
   */
  private void doTradingSell() {
    final boolean isStopOrderPlaced = (this.stopOrderCurrentOrder == null);

    // positionnement du stop order de secours si activé dans le fichier de configuration
    if (this.appProp.getTrader().getVente().getSecureStopOrder().getActivate() && isStopOrderPlaced) {
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
//        const sellMode = this.determineTradeSellMode();
//    switch (sellMode) {
//      case E_TRADESELLMODE.WAITING_FOR_BENEFICE:
//                const coursRequisPourBenefice = MathUtils
//              .calculateAddPourcent(Number(this.lastBuyOrder.price), this.pourcentBeforeStartVenteMode);
//        this.options.logger.log('debug', 'MODE VENTE - Not enougth benef. Waiting benefice to : ' + coursRequisPourBenefice);
//        break;
//      case E_TRADESELLMODE.BENEFICE:
//        this.options.logger.log('info', 'MODE VENTE - Benefice OK');
//        this.doTradingSellBenefice();
//        break;
//    }
  }


  /**
   * Fonction qui positionne un stopOrder a XX% en dessous du court actuel.
   * Le XX% est configurable dans le fichier de configuration
   */
  public void stopOrderPlace(final double price) {
    // si un stop order est deja present, il faut le supprimer
    if (this.stopOrderCurrentOrder != null) {
      this.orderService.cancelOrder(this.stopOrderCurrentOrder.getId());
      this.stopOrderCurrentOrder = null;
    }
    this.orderService.placeStopSellOrder(price, this.accountService.getBtc())
            .ifPresent(order -> this.stopOrderCurrentOrder = order);
  }
}
