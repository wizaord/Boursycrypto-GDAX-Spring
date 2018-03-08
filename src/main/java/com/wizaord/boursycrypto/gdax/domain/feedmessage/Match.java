package com.wizaord.boursycrypto.gdax.domain.feedmessage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wizaord.boursycrypto.gdax.domain.GenericFeedMessage;
import lombok.Data;

import java.util.Date;

/**
 * Message sent when an order is buy or sell to someone
 * {"type":"match","trade_id":1194234,"maker_order_id":"556e7032-a23a-4d95-9bb2-b0fb92b197a5",
 * "taker_order_id":"2c693229-0bbb-4d49-9323-37a1c128c110",
 * "side":"sell","size":"0.02002620","price":"9962.01000000",
 * "product_id":"BTC-USD","taker_user_id":"5a0072d571047e00bf56138a",
 * "user_id":"5a0072d571047e00bf56138a","taker_profile_id":"5362bd69-4817-45e9-8f85-ccf8c8df3332",
 * "profile_id":"5362bd69-4817-45e9-8f85-ccf8c8df3332","sequence":16014300,
 * "time":"2018-03-08T12:07:47.033000Z"}
 */
@Data
public class Match extends GenericFeedMessage {
    @JsonProperty("trade_id")
    private String tradeId;
    @JsonProperty("maker_order_id")
    private String makerOrderId;
    @JsonProperty("taker_order_id")
    private String takerOrderId;
    private String side;
    private Double size;
    private Double price;
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("taker_user_id")
    private String takerUserId;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("taker_profile_id")
    private String taker_profile_id;
    @JsonProperty("profile_id")
    private String profile_id;
    private String sequence;
    private Date time;
}
