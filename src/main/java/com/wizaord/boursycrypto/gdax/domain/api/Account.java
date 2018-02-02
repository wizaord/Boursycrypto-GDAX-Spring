package com.wizaord.boursycrypto.gdax.domain.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Account {
  private String id;            // Account ID
  private String currency;      // the currency of the account
  private BigDecimal balance;   // total funds in the account
  private BigDecimal available; // funds available to withdraw or trade
  private BigDecimal hold;      // funds on hold (not available for use)
  @JsonProperty("profile_id")
  private String profileId;
}
