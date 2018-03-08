package com.wizaord.boursycrypto.gdax.domain.feedmessage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wizaord.boursycrypto.gdax.domain.GenericFeedMessage;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SubscriptionMessage extends GenericFeedMessage {
}
