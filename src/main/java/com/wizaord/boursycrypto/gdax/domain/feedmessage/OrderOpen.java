package com.wizaord.boursycrypto.gdax.domain.feedmessage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wizaord.boursycrypto.gdax.domain.GenericFeedMessage;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * An open order is an order which has been open in the GDAX orderBook.
 * This order can be buy or sell by the GDAX platform
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OrderOpen extends GenericFeedMessage {
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

