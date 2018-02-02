package com.wizaord.boursycrypto.gdax.service;

import com.wizaord.boursycrypto.gdax.domain.E_TradingMode;
import com.wizaord.boursycrypto.gdax.domain.feedmessage.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TradeService {

  private static final Logger LOG = LoggerFactory.getLogger(TradeService.class);

  private Float lastCurrentPriceReceived;
  private Float currentPrice;
  private E_TradingMode traderMode = E_TradingMode.NOORDER;

  public void notifyNewTickerMessage(final Ticker ticMessage) {
      this.lastCurrentPriceReceived = ticMessage.getPrice().floatValue();
      LOG.info("New Ticker value {}", this.lastCurrentPriceReceived.floatValue());
  }

  /**
   * Algo mis en place.
   * Si pas encore de cours sur le prix, on ne fait rien
   * Si on est pas en mode VENTE, on ne fait rien
   * Si on est en mode VENTE
   *  - on regarde si un stopOrder est positionné. Si non, on le positionne a XX% en dessous du prix en cours (possible si lors du demarrage de l'application, le cours est tellement bas qu'on ne peut pas mettre le stopOrder)
   *  - on calcule la balance et on l'affiche
   *  - si on est en deficite, on ne change pas le stop order
   *  - si on est en bénéfice, on positionne le stopOrder juste pour gagner de l'argent
   *      - et ensuite on fait monter ce stopOrder en fonction de la courbe
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

//      switch (this.traderMode) {
//        case NOORDER:
//          LOG.info("MODE UNKNOWN- determination du mode de fonctionnement");
//          this.determineTradeMode();
//          break;
//        case ACHAT:
//          LOG.info("MODE ACHAT - cours {]", this.currentPrice);
//          this.doTradingBuy();
//          break;
//        case VENTE:
//          this.logVenteEvolution();
//          if (! Boolean(this.confService.configurationFile.application.trader.modeVisualisation)) {
//            this.options.logger.log('debug', 'MODE VENTE');
//            this.doTradingSell();
//          }
//          break;
//      }
    }

//  /**
//   * Fonction qui permet de determiner dans quel mode de fonctionnement on se trouve
//   */
//  private E_TradingMode determineTradeMode() {
//    // this.traderMode = E_TRADEMODE.ACHAT;
//    // on va verifier si on a pas encore des coins.
//    if (this.accountService.btc > 0) {
//      this.options.logger.log('info', 'CHECK MODE - coin in wallet <' + this.accountService.btc.toFixed(4) + '>. Looking for last buy order');
//      this.customOrder.getLastBuyFill().then((order) => {
//              this.options.logger.log('info', 'CHECK MODE - Find last order buy. Inject order in this AWESOME project');
//      this.notifyNewOrder(order);
//            });
//    } else {
//      this.options.logger.log('info', 'CHECK MODE - No Btc in wallet. Set en ACHAT MODE');
//      this.traderMode = E_TRADEMODE.ACHAT;
//    }
//  }
}
