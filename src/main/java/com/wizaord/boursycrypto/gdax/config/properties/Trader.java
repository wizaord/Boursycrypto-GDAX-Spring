package com.wizaord.boursycrypto.gdax.config.properties;

import lombok.Data;

@Data
public class Trader {
  private String modeVisualisation;
  private Vente vente;
  private Achat achat;
}
