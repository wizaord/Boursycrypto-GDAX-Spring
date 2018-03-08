package com.wizaord.boursycrypto.gdax.domain.feedmessage;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.Optional;

public enum E_FeedMessage {
    TICKER("ticker", Ticker.class),
    RECEIVED("received", OrderReceived.class),
    OPEN("open", OrderOpen.class),
    DONE("done",OrderDone.class),
    ACTIVATE("activate", OrderActivated.class),
    MATCH("match", Match.class),
    SUBSCRIPTION("subscriptions",SubscriptionMessage.class);

    /**
     * The value of the type parameter in the feed message
     */
    public String feedTypeName;
    /**
     * The class which implements the object
     */
    public JavaType javaType;

    /**
     * private constructor.
     *
     * @param name
     * @param implementedClass
     */
    E_FeedMessage(final String name, final Class implementedClass) {
        this.feedTypeName = name;
        this.javaType = TypeFactory.defaultInstance().constructFromCanonical(implementedClass.getCanonicalName());
    }

    /**
     * Get the {@link E_FeedMessage} based on the feed message type
     *
     * @param name
     * @return
     */
    public static Optional<E_FeedMessage> getByName(final String name) {
        for (E_FeedMessage eFeedMsg : E_FeedMessage.values()) {
            if (eFeedMsg.feedTypeName.equals(name)) {
                return Optional.of(eFeedMsg);
            }
        }
        return Optional.empty();
    }
    }
