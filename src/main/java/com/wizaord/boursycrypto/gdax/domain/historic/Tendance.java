package com.wizaord.boursycrypto.gdax.domain.historic;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class Tendance {
  private LocalDateTime beginDate;
  private LocalDateTime endDate;
  private double averagePrice;
  private String type;
  private double evolPrice;
  private double evolPourcentage;
  private double volumeEchangee;
}
