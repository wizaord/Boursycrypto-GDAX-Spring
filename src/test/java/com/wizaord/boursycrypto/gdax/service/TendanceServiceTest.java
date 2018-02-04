package com.wizaord.boursycrypto.gdax.service;

import com.wizaord.boursycrypto.gdax.ConfigurationMain;
import com.wizaord.boursycrypto.gdax.domain.HistorizedTic;
import com.wizaord.boursycrypto.gdax.domain.feedmessage.Ticker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConfigurationMain.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TendanceServiceTest {

  @Autowired
  private TendanceService tendanceService;

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

}