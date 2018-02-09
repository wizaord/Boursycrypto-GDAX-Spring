package com.wizaord.boursycrypto.gdax.service.trade;

import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import com.wizaord.boursycrypto.gdax.domain.historic.Tendance;
import com.wizaord.boursycrypto.gdax.service.TendanceService;
import com.wizaord.boursycrypto.gdax.service.notify.SlackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.wizaord.boursycrypto.gdax.utils.MathUtils.df;

@Service
public class TradeBuyService {

  public static final Logger LOG = LoggerFactory.getLogger(TradeBuyService.class);

  @Autowired
  private TendanceService tendanceService;
  @Autowired
  private ApplicationProperties applicationProperties;
  @Autowired
  private SlackService slackService;


  private LocalDateTime lastNotifyBuyMessage = LocalDateTime.of(2010, 1, 1, 1, 1);


  @Scheduled(fixedRateString = "${application.trader.delay}")
  public void checkTendanceForBuy() {
    // on regarde les tendances sur les dernières minutes,
    // Si elles sont toutes baissière et que la dernière est une grosse chute > 2% (sur la minutes)
    // on positionne un stopOrder d'achat
    // et on suit la courbe baissière
    final List<Tendance> lastEveryMinutesTendances = this.tendanceService.getLastEveryMinutesTendances(15);
    LOG.debug("Retrieve {} tendances", lastEveryMinutesTendances.size());
    this.tendanceAchatLog(lastEveryMinutesTendances);

    boolean lookingForBuy = false;
    double cumulEvolutionNegative = 0;

    while (lastEveryMinutesTendances.size() != 0 && !lookingForBuy) {
      final Tendance tendance = lastEveryMinutesTendances.remove(0);
      if (tendance.getEvolPourcentage() < 0.3) {
        // on cumule l'evolution
        cumulEvolutionNegative += tendance.getEvolPourcentage();
      } else {
        // on arrete tout
        break;
      }

      // si l'evolution est negative à XX pourcent on remonte une alerte
      if (cumulEvolutionNegative <= this.applicationProperties.getTrader().getAchat().getPourcentageChuteCoursStopOrder()) {
        lookingForBuy = true;
      }
    }

    if (lookingForBuy) {
      doTradingBuy(cumulEvolutionNegative);
    }
  }


  /**
   * Cette fonction est appelée quand une grosse chute est détectée
   */
  private void doTradingBuy(final double cumulEvolutionNegative) {

    // envoie d'un message de notification
    // uniquement si l'ancien message date d'il y a lontemps
    LocalDateTime currentDate = LocalDateTime.now().minusMinutes(10);
    if (currentDate.isAfter(this.lastNotifyBuyMessage)) {
      this.lastNotifyBuyMessage = LocalDateTime.now();
      final String message = "CHECK FOR ACHAT - Baisse du cours : " + this.applicationProperties.getProduct().getName() + " de " + df.format(cumulEvolutionNegative);
      slackService.postListChannel(message);
    }
  }

  private void tendanceAchatLog(final List<Tendance> tendances) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("================ TENDANCE ===================");
      tendances.forEach(tendance -> {
        LOG.debug("Date {} average: {} prix: {} %: {}",
                tendance.getBeginDate().getHour() + ":" + tendance.getBeginDate().getMinute(),
                df.format(tendance.getAveragePrice()),
                df.format(tendance.getEvolPrice()),
                df.format(tendance.getEvolPourcentage()));
      });
    }
  }
}
