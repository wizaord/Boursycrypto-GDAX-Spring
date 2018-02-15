package com.wizaord.boursycrypto.gdax.listener.weksocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import com.wizaord.boursycrypto.gdax.domain.auth.SignatureHeader;
import com.wizaord.boursycrypto.gdax.domain.feedmessage.SubscribeRequest;
import com.wizaord.boursycrypto.gdax.service.HandleFeedMessageService;
import com.wizaord.boursycrypto.gdax.service.SignatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

import static com.wizaord.boursycrypto.gdax.config.WebSocketConfiguration.GDAX_WEBSOCKET;

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
    @Autowired
    private WebSocketContainer webSocketContainer;

    private Session session;

    /**
     * Open the connection with the webSocket server
     */
    public void startConnection() {
        while(session == null || !session.isOpen()) {
            // start feed
            LOG.info("Connecting WebSocket to URL : {}", GDAX_WEBSOCKET);
            try {
                session = webSocketContainer.connectToServer(this, URI.create(GDAX_WEBSOCKET));
            } catch (DeploymentException | IOException e) {
                LOG.error("Unable to connect the webSocket. Wait and restart", e);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                    LOG.error("Unable to wait 10 seconds", e1);
                }
            }
        }
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        LOG.info("Sending subscribe request to the webSocket");


        final SubscribeRequest subscriberequest = SubscribeRequest.builder()
                .type("subscribe")
                .product_id(applicationProperties.getProduct().getName())
//            .channel("full")
                .channel("ticker")
                .channel("user")
                .build();

        final String subscribeJson = jsonMapper.writeValueAsString(subscriberequest);

        final SignatureHeader signature = signatureService.getSignature("", "GET", subscribeJson);

        //add auth data
        subscriberequest.setTimestamp(signature.getCbAccessTimestamp());
        subscriberequest.setKey(signature.getCbAccessKey());
        subscriberequest.setPassphrase(signature.getCbAccessPassphrase());
        subscriberequest.setSignature(signature.getCbAccessSign());

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
    public void processClose(Session session, CloseReason reason) throws IOException {
        LOG.error("WebSocket has been close with reason " + reason.toString());
        this.startConnection();
    }

    @OnError
    public void processError(Throwable t) {
        t.printStackTrace();
    }

}
