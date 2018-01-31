package com.wizaord.boursycrypto.gdax.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import com.wizaord.boursycrypto.gdax.domain.SubscribeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;

import static com.wizaord.boursycrypto.gdax.config.WebSocketConfiguration.GDAX_WEBSOCKET;

@Service
@ClientEndpoint
public class GDaxWebSocketService {

  private static final Logger LOG = LoggerFactory.getLogger(GDaxWebSocketService.class);

  @Autowired
  private ObjectMapper jsonMapper;

  @Autowired
  private SignatureService signatureService;

  @Autowired
  private ApplicationProperties applicationProperties;


  @OnOpen
  public void onOpen(Session session) throws IOException {
    LOG.info("Sending subscribe request to the webSocket");

    final String timestamp = String.valueOf(Instant.now().getEpochSecond());

    final SubscribeRequest subscriberequest = SubscribeRequest.builder()
            .type("subscribe")
            .product_id(applicationProperties.getProduct().getName())
            .channel("full")
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

//    auth: {
//      key: confService.configurationFile.application.auth.apikey,
//              secret: confService.configurationFile.application.auth.apisecretkey,
//              passphrase: confService.configurationFile.application.auth.passphrase,
//    },
    session.getBasicRemote().sendText(subscribeMsg);
  }

  @OnMessage
  public void processMessage(String message) {
    System.out.println("Received message in client: " + message);
  }

  @OnClose
  public void processClose(Session session, CloseReason reason) {
  }

  @OnError
  public void processError(Throwable t) {
    t.printStackTrace();
  }

}
