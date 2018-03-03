package com.wizaord.boursycrypto.gdax.domain.feedmessage;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class E_FeedMessageTest {

    @Test
    public void getExistingEnumByName() {
        assertThat(E_FeedMessage.getByName(E_FeedMessage.TICKER.feedTypeName)).isNotEmpty();
    }

    @Test
    public void getNonExistingEnumByName() {
        assertThat(E_FeedMessage.getByName("NONEXIST")).isEmpty();
    }

}