package com.wizaord.boursycrypto.gdax.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GenericFeedMessage {
  protected String type;
}
