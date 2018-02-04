package com.wizaord.boursycrypto.gdax.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Builder
@Data

public class HistorizedTic {
  private LocalDateTime generatedDate;
  private long nbTic;
  private double averagePrice;
  private double volumeEchange;
  private double minPrice;
  private double maxPrice;
  private long nbBuy;
  private long nbSell;
}
