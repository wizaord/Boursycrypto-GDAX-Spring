package com.wizaord.boursycrypto.gdax.domain.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Builder
@Data
public class Order {
  private String id;
  private BigDecimal price;
  private BigDecimal size;
  @JsonProperty("product_id")
  private String product_id;
  private String side;
  private String stp;
  private String type;
  @JsonProperty("time_in_force")
  private String time_in_force;
  @JsonProperty("post_only")
  private boolean post_only;
  @JsonProperty("created_at")
  private Date create_at;
  @JsonProperty("fill_fees")
  private BigDecimal fill_fees;
  @JsonProperty("filled_size")
  private BigDecimal filled_size;
  @JsonProperty("executed_value")
  private BigDecimal executed_value;
  private String status;
  private boolean settled;
  private String stop;
  @JsonProperty("stop_price")
  private BigDecimal stop_price;
}
