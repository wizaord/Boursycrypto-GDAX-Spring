package com.wizaord.boursycrypto.gdax.config.properties;

import lombok.Data;

@Data
public class Achat {
  private Boolean activate;
  private Integer maxBuyAmount;
  private Float pourcentageChuteCoursStopOrder;
}
