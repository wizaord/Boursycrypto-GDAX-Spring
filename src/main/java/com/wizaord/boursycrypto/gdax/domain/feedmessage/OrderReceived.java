package com.wizaord.boursycrypto.gdax.domain.feedmessage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wizaord.boursycrypto.gdax.domain.GenericFeedMessage;
import lombok.Data;

import java.util.Date;

/**
 * A received order means that GDAX has received a new order. This order is not yet added in the orderBook.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OrderReceived extends GenericFeedMessage{
    @JsonProperty("order_id")
    private String orderId;
    @JsonProperty("order_type")
    private String OrderType;
    private Float size;
    private Float price;
    private String side;
    private Date time;
}
