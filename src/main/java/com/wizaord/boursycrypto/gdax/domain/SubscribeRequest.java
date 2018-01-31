package com.wizaord.boursycrypto.gdax.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class SubscribeRequest {
  private String type;              // The message type : "SUBSCRIPTION"
  @Singular
  private List<String> product_ids; // The list of productsIds : "ETH-EUR", ...
  @Singular
  private List<String> channels;    // The list of channels : ticker, level2, user, ....

  @JsonIgnore
  private String signature;         // The base64-encoded signature (see Signing a Message).
  @JsonIgnore private String key;               // The api key as a string.
  @JsonIgnore private String timestamp;         // A timestamp for your request.
  @JsonIgnore private String passphrase;        // The passphrase you specified when creating the API key.
}
