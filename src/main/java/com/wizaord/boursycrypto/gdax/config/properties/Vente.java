package com.wizaord.boursycrypto.gdax.config.properties;

import lombok.Data;

@Data
public class Vente {
  private Start start;
  private SecureStopOrder secureStopOrder;
  private Benefice benefice;
}
