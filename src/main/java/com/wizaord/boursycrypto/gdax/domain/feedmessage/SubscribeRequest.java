package com.wizaord.boursycrypto.gdax.domain.feedmessage;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeRequest {
    private String key;               // The api key as a string.
    private String signature;         // The base64-encoded signature (see Signing a Message).
    private String timestamp;         // A timestamp for your request.
    private String passphrase;        // The passphrase you specified when creating the API key.

    protected String type;              // The message type : "SUBSCRIPTION"
    @Singular
    protected List<String> product_ids; // The list of productsIds : "ETH-EUR", ...
    @Singular
    protected List<String> channels;    // The list of channels : ticker, level2, user, ....


}
