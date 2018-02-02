package com.wizaord.boursycrypto.gdax.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Fill {
  @JsonProperty("created_at")
  private Date created_at;
  @JsonProperty("trade_id")
  private long trade_id;
  @JsonProperty("product_id")
  private String product_id;
  @JsonProperty("order_id")
  private String order_id;
  @JsonProperty("user_id")
  private String user_id;
  @JsonProperty("profile_id")
  private String profile_id;
  private String liquidity;
  private BigDecimal price;
  private BigDecimal size;
  private BigDecimal fee;
  private String side;
  private String settled;


  public Order mapToOrder() {
    return Order.builder()
            .id(this.order_id)
            .price(this.price)
            .side(this.side)
            .fill_fees(this.fee)
            .size(this.size)
            .create_at(this.created_at)
            .build();
  }
}
