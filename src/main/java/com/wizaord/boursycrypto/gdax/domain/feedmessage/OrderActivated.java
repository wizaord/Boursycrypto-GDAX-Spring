package com.wizaord.boursycrypto.gdax.domain.feedmessage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wizaord.boursycrypto.gdax.domain.GenericFeedMessage;
import com.wizaord.boursycrypto.gdax.domain.api.Order;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * An activate order is passed for stopOrder.
 * When the order is posted in GDAX, the message is sent.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderActivated extends GenericFeedMessage {

    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("order_id")
    private String orderId;
    @JsonProperty("stop_type")
    private String stopType;
    private String side;
    @JsonProperty("stop_price")
    private BigDecimal stopPrice;
    @JsonProperty("limit_price")
    private BigDecimal limitPrice;
    private BigDecimal size;
    @JsonProperty("taker_fee_rate")
    private BigDecimal takerFeeRate;
    private Date time;

    /**
     * Default consctructor
     */
    public OrderActivated() {

    }

    /**
     * Constructor from ApiOrder
     * @param order
     */
    public OrderActivated(Order order) {
        super();
        this.productId = order.getProduct_id();
        this.orderId = order.getId();
        this.stopType = order.getType();
        this.side = order.getSide();
        this.stopPrice = order.getStop_price();
        if (order.getPrice() != null) {
            this.limitPrice = order.getPrice();
        }
        this.size = order.getSize();
        this.takerFeeRate = order.getFill_fees();
        this.time = order.getCreate_at();
    }
}
