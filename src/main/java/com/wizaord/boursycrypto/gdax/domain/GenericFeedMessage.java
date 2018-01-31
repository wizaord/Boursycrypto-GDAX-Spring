package com.wizaord.boursycrypto.gdax.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericFeedMessage {
  private String type;
}
