package com.wizaord.boursycrypto.gdax.service;

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

  private DecimalFormat df = new DecimalFormat("#.##");
  private Double lastCurrentPriceReceived;
  private double currentPrice;
  private E_TradingMode traderMode = E_TradingMode.NOORDER;
  private Order lastOrder;

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
        //        if (!Boolean(this.confService.configurationFile.application.trader.modeVisualisation)) {
        //          this.options.logger.log('debug', 'MODE VENTE');
        //          this.doTradingSell();
        //        }
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
    this.lastOrder = order;
    this.traderMode = VENTE;
  }

  public void logVenteEvolution() {
    final double fee = this.lastOrder.getFill_fees().doubleValue();
    final double price = this.lastOrder.getPrice().doubleValue();
    final double evolution = MathUtils.calculatePourcentDifference(this.currentPrice, this.lastOrder.getPrice().doubleValue());

    String message = "COURS EVOL : - achat " + df.format(price) + " - fee " + df.format(fee) + " - now " + this.currentPrice;
    message += " - benefice " + df.format(this.getBalance(this.currentPrice)) + "€ - evolution " + df.format(evolution) + "%";
    LOG.info(message);
  }


  public double getBalance(final double currentPrice) {
    final double lastOrderPrice = this.lastOrder.getPrice().doubleValue();
    final double quantity = this.lastOrder.getSize().doubleValue();
    final double feeAchat = this.lastOrder.getFill_fees().doubleValue();
    final double feeVente = quantity * currentPrice * 0.0025;

    final double prixVente = (quantity * currentPrice) - feeVente;
    final double coutAchat = (quantity * lastOrderPrice) + feeAchat;
    return prixVente - coutAchat;
  }
}
