package com.wizaord.boursycrypto.gdax.domain.feedmessage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wizaord.boursycrypto.gdax.domain.GenericFeedMessage;
import lombok.Data;

import java.util.Date;

/**
 * An activate order is passed for stopOrder.
 * When the order is posted in GDAX, the message is sent.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OrderActivated extends GenericFeedMessage {

    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("order_id")
    private String orderId;
    @JsonProperty("stop_type")
    private String stopType;
    private String side;
    @JsonProperty("stop_price")
    private Float stopPrice;
    @JsonProperty("limit_price")
    private Float limitPrice;
    private Float size;
    @JsonProperty("taker_fee_rate")
    private Float takerFeeRate;
    private Date time;
}
