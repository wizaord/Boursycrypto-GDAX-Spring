package com.wizaord.boursycrypto.gdax.service;

import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import com.wizaord.boursycrypto.gdax.domain.HistorizedTic;
import com.wizaord.boursycrypto.gdax.domain.feedmessage.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
public class TendanceService {
  private static final Logger LOG = LoggerFactory.getLogger(TendanceService.class);

  @Autowired
  private ApplicationProperties applicationProperties;

  private List<Ticker> tickerList = new ArrayList<>();
  private LinkedList<HistorizedTic> historizedTics = new LinkedList<>();

  public void notifyTickerMessage(final Ticker tic) {
    this.tickerList.add(tic);
  }

  @Scheduled(fixedRateString = "${application.historique.computeDelay}")
  public void computeTradeMessagesInHistoriqueCompute() {

    LOG.debug("Launching compute Tics message in HistoricTic");
    HistorizedTic historicTic = HistorizedTic.builder()
            .generatedDate(LocalDateTime.now().truncatedTo(SECONDS))
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
      historicTic.setGeneratedDate(LocalDateTime.now().truncatedTo(SECONDS));
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

}
