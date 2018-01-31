package com.wizaord.boursycrypto.gdax.config.properties;

import lombok.Data;

@Data
public class Historique {
  private Boolean logTendance;
  private Integer computeDelay;
  private Integer maxHistoriqueComputeKeepInMemory;
}
