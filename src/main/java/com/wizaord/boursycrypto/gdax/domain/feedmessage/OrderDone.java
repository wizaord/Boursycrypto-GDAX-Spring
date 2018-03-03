package com.wizaord.boursycrypto.gdax.domain.feedmessage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wizaord.boursycrypto.gdax.domain.GenericFeedMessage;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * This order is executed when an order is finished. Two rasean are possible :
 *  - filled : if the order has been executed by GDAX
 *  - canceled : if the user (or maybe something else) has canceled this order
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OrderDone extends GenericFeedMessage {
    private Date time;
    @JsonProperty("product_id")
    private String productId;
    private String sequence;
    private BigDecimal price;
    @JsonProperty("order_id")
    private String orderId;
    private String reason;
    private String side;
    @JsonProperty("remaining_size")
    private Float remainingSize;
}
