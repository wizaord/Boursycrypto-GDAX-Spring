package com.wizaord.boursycrypto.gdax.service;

import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import com.wizaord.boursycrypto.gdax.domain.feedmessage.Ticker;
import com.wizaord.boursycrypto.gdax.domain.historic.HistorizedTic;
import com.wizaord.boursycrypto.gdax.domain.historic.Tendance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
public class TendanceService {
  private static final Logger LOG = LoggerFactory.getLogger(TendanceService.class);
  List<Ticker> tickerList = new ArrayList<>();
  LinkedList<HistorizedTic> historizedTics = new LinkedList<>();
  @Autowired
  private ApplicationProperties applicationProperties;

  public void notifyTickerMessage(final Ticker tic) {
    this.tickerList.add(tic);
  }

  @Scheduled(fixedRateString = "${application.historique.computeDelay}")
  public void computeTradeMessagesInHistoriqueCompute() {

    LOG.debug("Launching compute Tics message in HistoricTic");
    HistorizedTic historicTic = HistorizedTic.builder()
            .generatedDate(LocalDateTime.now().truncatedTo(MINUTES))
            .nbTic(0)
            .averagePrice(0)
            .volumeEchange(0)
            .minPrice(0)
            .maxPrice(0)
            .nbBuy(0)
            .nbSell(0)
            .build();
    double totalPrice = 0;

    if (this.tickerList.isEmpty()) {
      if (this.historizedTics.isEmpty()) {
        LOG.info("No Ticker received. Wait again to calculate historic");
        return;
      }
      historicTic = this.historizedTics.getLast();
      historicTic.setGeneratedDate(LocalDateTime.now().truncatedTo(MINUTES));
      historicTic.setNbSell(0);
      historicTic.setNbBuy(0);
      historicTic.setNbTic(0);
      historicTic.setVolumeEchange(0);
    } else {
      List<Ticker> copiedList = this.tickerList;
      this.tickerList = new ArrayList<>();

      for (Ticker tic : copiedList) {
        historicTic.setNbTic(historicTic.getNbTic() + 1);
        if (tic.getLastSize() != null) {
          historicTic.setVolumeEchange(historicTic.getVolumeEchange() + tic.getLastSize().doubleValue());
        }
        if (tic.getSide() != null) {
          if (tic.getSide().equals("buy")) {
            historicTic.setNbBuy(historicTic.getNbBuy() + 1);
          } else {
            historicTic.setNbSell(historicTic.getNbSell() + 1);
          }
        }

        if (historicTic.getMinPrice() > tic.getPrice().doubleValue() || historicTic.getMinPrice() == 0) {
          historicTic.setMinPrice(tic.getPrice().doubleValue());
        }
        if (historicTic.getMaxPrice() < tic.getPrice().doubleValue() || historicTic.getMaxPrice() == 0) {
          historicTic.setMaxPrice(tic.getPrice().doubleValue());
        }
        totalPrice += tic.getPrice().doubleValue();

      }
      historicTic.setAveragePrice(totalPrice / historicTic.getNbTic());
    }

    this.historizedTics.add(historicTic);
    LOG.debug("Added new HistoricTic : {}", historicTic);

    // remove old HistoriqueTendance
    while (this.historizedTics.size() > this.applicationProperties.getHistorique().getMaxHistoriqueComputeKeepInMemory()) {
      this.historizedTics.removeFirst();
    }
  }


  /**
   * Le calcul d'une tendance est simple.
   * On prend la date de Debut, on prend la date de fin et on compare
   *
   * @param {Date} beginDate
   * @param {Date} endDate
   *
   * @returns {number}
   */
  Optional<Tendance> calculeTendance(final LocalDateTime beginDate, LocalDateTime endDate) {
    if (endDate == null) {
      endDate = LocalDateTime.now();
    }

    if (this.historizedTics.isEmpty()) {
      return Optional.empty();
    }

    final LocalDateTime firstListDate = this.historizedTics.peekFirst().getGeneratedDate();
    final LocalDateTime beginDateSec = beginDate.truncatedTo(MINUTES);
    final LocalDateTime endDateSec = endDate.truncatedTo(MINUTES);

    // si la date de debut n'existe pas, on ne remonte pas de tendance
    // console.log('First date ' + firstListDate.getTime());
    // console.log('Begin Date ' + beginDateSec.getTime());
    // console.log('End date ' + endDateSec.getTime());
    if (firstListDate.isAfter(beginDateSec)) {
      return Optional.empty();
    }

    final LinkedList<HistorizedTic> historiqueTicsInInterval = new LinkedList<>(this.getHistoriqueTics(beginDateSec, endDateSec));
    if (historiqueTicsInInterval.size() == 0) {
      LOG.info("Unable to get computeHisto between beginDate {} and endDate {}", beginDateSec, endDateSec);
      return Optional.empty();
    }

    final HistorizedTic oldElement = historiqueTicsInInterval.peekFirst();
    final HistorizedTic lastElement = historiqueTicsInInterval.peekLast();


    final Tendance tendance = Tendance.builder()
            .beginDate(oldElement.getGeneratedDate())
            .endDate(lastElement.getGeneratedDate())
            .evolPrice(lastElement.getAveragePrice() - oldElement.getAveragePrice())
            .evolPourcentage(0)
            .type("")
            .volumeEchangee(0)
            .averagePrice(0)
            .build();

    // set average price
    tendance.setAveragePrice(historiqueTicsInInterval.stream()
            .map(HistorizedTic::getAveragePrice)
            .reduce((previous, current) -> previous + current).orElse(0D) / historiqueTicsInInterval.size());

    // set type
    tendance.setType((tendance.getEvolPrice() >= 0) ? "HAUSSE" : "BAISSE");

    //  (Valeur d’arrivée – Valeur de départ) / Valeur de départ x 100);
    tendance.setEvolPourcentage(((lastElement.getAveragePrice() - oldElement.getAveragePrice()) / oldElement.getAveragePrice()) * 100);

    // set volument echange
    tendance.setVolumeEchangee(historiqueTicsInInterval.stream().mapToDouble(HistorizedTic::getVolumeEchange).sum());
    return Optional.of(tendance);
  }

  /**
   * Retourne la liste des historiques entre la date de debut et la date de fin
   *
   * @param {Date} beginDate
   * @param {Date} endDate
   *
   * @returns {HistoriqueCompute[]}
   */
  List<HistorizedTic> getHistoriqueTics(final LocalDateTime beginDate, final LocalDateTime endDate) {
    return this.historizedTics.stream()
            .filter(historizedTic ->
             ((historizedTic.getGeneratedDate().isEqual(beginDate) || historizedTic.getGeneratedDate().isAfter(beginDate))
                && (historizedTic.getGeneratedDate().isEqual(endDate) || historizedTic.getGeneratedDate().isBefore(endDate)))
            )
            .sorted(Comparator.comparing(HistorizedTic::getGeneratedDate))
            .collect(Collectors.toList());
  }

  public List<Tendance> getLastEveryMinutesTendances(final int nbTendance) {
    final List<Tendance> tendances = new ArrayList<>(nbTendance);

    if (this.historizedTics.isEmpty()) {
      LOG.debug("No historicTic for the moment. Please wait");
      return tendances;
    }

    final LocalDateTime lastDate = this.historizedTics.peekLast().getGeneratedDate();
    for (int i = 0 ; i < this.historizedTics.size(); i++) {
      final LocalDateTime endDate = lastDate.minusMinutes(i);
      final LocalDateTime beginDate = lastDate.minusMinutes((i + 1));
      final Optional<Tendance> calculatedTendance = this.calculeTendance(beginDate, endDate);
      if(calculatedTendance.isPresent()) {
        tendances.add(calculatedTendance.get());
      }
    }
    return tendances;
  }
}
