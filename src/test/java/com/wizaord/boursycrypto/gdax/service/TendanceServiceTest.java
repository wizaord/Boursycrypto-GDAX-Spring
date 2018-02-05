package com.wizaord.boursycrypto.gdax.service;

import com.wizaord.boursycrypto.gdax.ConfigurationMain;
import com.wizaord.boursycrypto.gdax.domain.HistorizedTic;
import com.wizaord.boursycrypto.gdax.domain.Tendance;
import com.wizaord.boursycrypto.gdax.domain.feedmessage.Ticker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConfigurationMain.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TendanceServiceTest {

  @Autowired
  private TendanceService tendanceService;

  @Before
  public void cleanContext() {
    this.tendanceService.historizedTics.clear();
    this.tendanceService.tickerList.clear();
  }

  @Test
  public void addTicTest() {
    assertThat(this.tendanceService.tickerList).hasSize(0);
    final Ticker tic = Ticker.builder()
            .price(BigDecimal.valueOf(100))
            .build();
    this.tendanceService.notifyTickerMessage(tic);
    assertThat(this.tendanceService.tickerList).hasSize(1);
  }


  @Test
  public void computeTicListTest() {
    this.tendanceService.notifyTickerMessage(Ticker.builder().price(BigDecimal.valueOf(100)).side("buy").lastSize(BigDecimal.valueOf(1)).build());
    this.tendanceService.notifyTickerMessage(Ticker.builder().price(BigDecimal.valueOf(80)).side("buy").lastSize(BigDecimal.valueOf(1)).build());
    this.tendanceService.notifyTickerMessage(Ticker.builder().price(BigDecimal.valueOf(120)).side("buy").lastSize(BigDecimal.valueOf(1)).build());

    this.tendanceService.computeTradeMessagesInHistoriqueCompute();

    assertThat(this.tendanceService.historizedTics).hasSize(1);
    assertThat(this.tendanceService.tickerList).hasSize(0);
    final HistorizedTic historicTic = this.tendanceService.historizedTics.getFirst();
    assertThat(historicTic).isNotNull();
    assertThat(historicTic.getMaxPrice()).isEqualTo(120);
    assertThat(historicTic.getMinPrice()).isEqualTo(80);
    assertThat(historicTic.getNbTic()).isEqualTo(3);
    assertThat(historicTic.getNbSell()).isEqualTo(0);
    assertThat(historicTic.getNbBuy()).isEqualTo(3);
    assertThat(historicTic.getVolumeEchange()).isEqualTo(3);
    assertThat(historicTic.getAveragePrice()).isEqualTo(100);
  }

  @Test
  public void computeAndCleanHistoricList() {
    for(int i = 0; i < 1000; i++) {
      this.tendanceService.notifyTickerMessage(Ticker.builder().price(BigDecimal.valueOf(100)).side("buy").lastSize(BigDecimal.valueOf(1)).build());
      this.tendanceService.computeTradeMessagesInHistoriqueCompute();
    }

    assertThat(this.tendanceService.historizedTics).hasSize(100);
  }

  @Test
  public void getHistoriqueTicsTest() {
    // added 4 historicTic
    this.tendanceService.historizedTics.add(HistorizedTic.builder().averagePrice(100).generatedDate(LocalDateTime.of(2014, Month.JANUARY, 1, 10, 10, 00)).build());
    this.tendanceService.historizedTics.add(HistorizedTic.builder().averagePrice(101).generatedDate(LocalDateTime.of(2014, Month.JANUARY, 1, 10, 11, 00)).build());
    this.tendanceService.historizedTics.add(HistorizedTic.builder().averagePrice(102).generatedDate(LocalDateTime.of(2014, Month.JANUARY, 1, 10, 12, 00)).build());
    this.tendanceService.historizedTics.add(HistorizedTic.builder().averagePrice(103).generatedDate(LocalDateTime.of(2014, Month.JANUARY, 1, 10, 13, 00)).build());

    final LocalDateTime beginDate = LocalDateTime.of(2014, Month.JANUARY, 1, 10, 11, 01);
    final LocalDateTime endDate = LocalDateTime.of(2014, Month.JANUARY, 1, 10, 12, 59);

    final List<HistorizedTic> historiqueTics = this.tendanceService.getHistoriqueTics(beginDate, endDate);
    assertThat(historiqueTics).hasSize(1);
    final HistorizedTic historizedTic = historiqueTics.get(0);
    assertThat(historizedTic.getAveragePrice()).isEqualTo(102);
  }

  @Test
  public void calculeTendanceTest() {
    this.tendanceService.historizedTics.add(HistorizedTic.builder().averagePrice(100).generatedDate(LocalDateTime.of(2014, Month.JANUARY, 1, 10, 10, 00)).build());
    this.tendanceService.historizedTics.add(HistorizedTic.builder().averagePrice(101).generatedDate(LocalDateTime.of(2014, Month.JANUARY, 1, 10, 11, 00)).build());
    this.tendanceService.historizedTics.add(HistorizedTic.builder().averagePrice(102).generatedDate(LocalDateTime.of(2014, Month.JANUARY, 1, 10, 12, 00)).build());
    this.tendanceService.historizedTics.add(HistorizedTic.builder().averagePrice(103).generatedDate(LocalDateTime.of(2014, Month.JANUARY, 1, 10, 13, 00)).build());

    final LocalDateTime beginDate = LocalDateTime.of(2014, Month.JANUARY, 1, 10, 11, 01);

    final Optional<Tendance> tendance = this.tendanceService.calculeTendance(beginDate, null);
    assertThat(tendance).isNotNull();
    assertThat(tendance.isPresent()).isTrue();
    assertThat(tendance.get().getAveragePrice()).isEqualTo(102.5D);
  }

}