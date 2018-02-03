package com.wizaord.boursycrypto.gdax.domain.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class PlaceOrder {
  private String type;  // limit or market
  private String side;  // buy or sell
  @JsonProperty("product_id")
  private String productId;
  private String size;
  private String price;
  private String stop;
  @JsonProperty("stop_price")
  private String stopPrice;   // Only if stop is defined. Sets trigger price for stop order.
}

