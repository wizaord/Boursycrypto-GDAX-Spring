package com.wizaord.boursycrypto.gdax.domain;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.wizaord.boursycrypto.gdax.domain.feedmessage.Ticker;

import java.util.Optional;

public enum E_FeedMessage {
  TICKER("ticker", Ticker.class),
  OPEN("open", GenericFeedMessage.class),
  DONE("done", GenericFeedMessage.class),
  SUBSCRIPTION("subscriptions", GenericFeedMessage.class),
  RECEIVED("received", GenericFeedMessage.class);

  /**
   * The value of the type parameter in the feed message
   */
  public String tickerName;
  /**
   * The class which implements the object
   */
  public JavaType javaType;

  /**
   * private constructor.
   * @param name
   * @param implementedClass
   */
  E_FeedMessage(final String name, final Class implementedClass) {
    this.tickerName = name;
    this.javaType = TypeFactory.defaultInstance().constructFromCanonical(implementedClass.getCanonicalName());
  }

  /**
   * Get the {@link E_FeedMessage} based on the feed message type
   * @param name
   * @return
   */
  public static Optional<E_FeedMessage> getByName(final String name) {
    for (E_FeedMessage eFeedMsg : E_FeedMessage.values()) {
      if (eFeedMsg.tickerName.equals(name)) {
        return Optional.of(eFeedMsg);
      }
    }
    return Optional.empty();
  }
}
