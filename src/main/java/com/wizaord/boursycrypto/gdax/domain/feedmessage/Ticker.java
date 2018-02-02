package com.wizaord.boursycrypto.gdax.domain.feedmessage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wizaord.boursycrypto.gdax.domain.GenericFeedMessage;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Date;

/**
 *The ticker channel provides real-time price updates every time a match happens.
 * It batches updates in case of cascading matches, greatly reducing bandwidth requirements.
 *
 *
 * {
 "type": "ticker",
 "sequence": 3291341298,
 "product_id": "BTC-EUR",
 "price": "8052.94000000",
 "open_24h": "8189.14000000",
 "volume_24h": "3377.29740495",
 "low_24h": "8052.94000000",
 "high_24h": "8320.56000000",
 "volume_30d": "122663.46798941",
 "best_bid": "8052.15",
 "best_ask": "8052.94",
 "side": "buy",
 "time": "2018-01-31T21:26:20.947000Z",
 "trade_id": 10960276,
 "last_size": "0.01000000"
 }
 */
@Value
public class Ticker extends GenericFeedMessage{
  private String sequence;
  @JsonProperty("product_id")
  private String productId;
  private BigDecimal price;
  @JsonProperty("open_24h")
  private BigDecimal open24h;
  @JsonProperty("volume_24h")
  private BigDecimal volume24h;
  @JsonProperty("low_24h")
  private BigDecimal low24h;
  @JsonProperty("volume_30d")
  private BigDecimal volume30d;
  @JsonProperty("best_bid")
  private Float bestBid;
  @JsonProperty("best_ask")
  private Float bestAsk;
  private String side;
  private Date time;
  @JsonProperty("trade_id")
  private String tradeId;
  @JsonProperty("lasy_size")
  private BigDecimal lastSize;
}

