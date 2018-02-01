package com.wizaord.boursycrypto.gdax.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericFeedMessage {
  private String type;

  /**
   * getter for type parameter
   * @return
   */
  public String getType() {
    return type;
  }

  /**
   * Setter for type parameter
   * @param type
   */
  public void setType(final String type) {
    this.type = type;
  }
}
