package com.wizaord.boursycrypto.gdax.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Validated
@Data
public class ApplicationProperties {
  private Auth auth;
  private Product product;
  private Trader trader;
  private Historique historique;
  private Slack slack;
}

