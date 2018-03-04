package com.wizaord.boursycrypto.gdax.domain.feedmessage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wizaord.boursycrypto.gdax.domain.GenericFeedMessage;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * An open order is an order which has been open in the GDAX orderBthisk.
 * This order can be buy or sell by the GDAX platform
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OrderOpen extends GenericFeedMessage {
    private Date time;
    @JsonProperty("product_id")
    private String productId;
    private BigDecimal price;
    @JsonProperty("order_id")
    private String orderId;
    private String side;
    @JsonProperty("remaining_size")
    private BigDecimal remainingSize;

    /**
     * Default constructor
     */
    public OrderOpen() {
        super();
    }

    /**
     * Constructor from an {@link OrderActivated}
     * @param oa
     */
    public OrderOpen(final OrderActivated oa) {
        super();
        this.time = oa.getTime();
        this.productId = oa.getProductId();
        if (oa.getStopPrice() != null) {
            this.price = oa.getStopPrice();
        }
        if (oa.getLimitPrice() != null) {
            this.price = oa.getLimitPrice();
        }
        this.orderId = oa.getOrderId();
        this.side = oa.getSide();
        this.remainingSize = oa.getSize();
        
    }
}

