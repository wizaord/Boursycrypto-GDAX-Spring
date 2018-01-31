package com.wizaord.boursycrypto.gdax.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import com.wizaord.boursycrypto.gdax.domain.SubscribeRequest;
import com.wizaord.boursycrypto.gdax.service.HandleFeedMessageService;
import com.wizaord.boursycrypto.gdax.service.SignatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.*;
import java.io.IOException;
import java.time.Instant;

@Service
@ClientEndpoint
public class FeedListener {

  private static final Logger LOG = LoggerFactory.getLogger(FeedListener.class);

  @Autowired
  private ObjectMapper jsonMapper;

  @Autowired
  private SignatureService signatureService;

  @Autowired
  private ApplicationProperties applicationProperties;

  @Autowired
  private HandleFeedMessageService handleFeedMessageService;


  @OnOpen
  public void onOpen(Session session) throws IOException {
    LOG.info("Sending subscribe request to the webSocket");

    final String timestamp = String.valueOf(Instant.now().getEpochSecond());

    final SubscribeRequest subscriberequest = SubscribeRequest.builder()
            .type("subscribe")
            .product_id(applicationProperties.getProduct().getName())
            .channel("full")
            .channel("ticker")
            .channel("user")
            .build();

    final String subscribeJson = jsonMapper.writeValueAsString(subscriberequest);

    //add auth data
    subscriberequest.setTimestamp(String.valueOf(timestamp));
    subscriberequest.setKey(applicationProperties.getAuth().getApikey());
    subscriberequest.setPassphrase(applicationProperties.getAuth().getPassphrase());
    subscriberequest.setSignature(signatureService.generate("", "GET", subscribeJson, timestamp));

//    String subscribeMsg = "{\"type\": \"subscribe\",\"product_ids\": [\"ETH-EUR\"],\"channels\": [\"ticker\", \"user\"]}";
    final String subscribeMsg = jsonMapper.writeValueAsString(subscriberequest);
    LOG.debug("Sending subscribe request : {}", subscribeMsg);
    session.getBasicRemote().sendText(subscribeMsg);
  }

  @OnMessage
  public void processMessage(String message) {
    LOG.debug("GDAX FEED : receive message : {}", message);
    this.handleFeedMessageService.handleMessage(message);
  }

  @OnClose
  public void processClose(Session session, CloseReason reason) {
  }

  @OnError
  public void processError(Throwable t) {
    t.printStackTrace();
  }

}
