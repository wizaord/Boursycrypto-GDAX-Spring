package com.wizaord.boursycrypto.gdax.config.properties;

import lombok.Data;

@Data
public class Trader {
  private Boolean modeVisualisation;
  private Vente vente;
  private Achat achat;
}
