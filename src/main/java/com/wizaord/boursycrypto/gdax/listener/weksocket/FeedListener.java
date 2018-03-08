package com.wizaord.boursycrypto.gdax.listener.weksocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import com.wizaord.boursycrypto.gdax.domain.auth.SignatureHeader;
import com.wizaord.boursycrypto.gdax.domain.feedmessage.SubscribeRequest;
import com.wizaord.boursycrypto.gdax.service.MessageDispatcherService;
import com.wizaord.boursycrypto.gdax.service.SignatureService;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.wizaord.boursycrypto.gdax.config.WebSocketConfiguration.GDAX_WEBSOCKET;

@Component
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
    private MessageDispatcherService handleFeedMessageService;
    @Autowired
    private WebSocketContainer webSocketContainer;


    /**
     * Open the connection with the webSocket server
     */
    public void startConnection() {
        ClientManager client = ClientManager.createClient(webSocketContainer);
        client.getProperties().put(ClientProperties.RECONNECT_HANDLER, new ReconnectHandler());

        LOG.info("Connecting WebSocket to URL : {}", GDAX_WEBSOCKET);
        try {
            client.connectToServer(this, new URI(GDAX_WEBSOCKET));
        } catch (DeploymentException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        LOG.info("Sending subscribe request to the webSocket");

        final SubscribeRequest subscriberequest = SubscribeRequest.builder()
                .type("subscribe")
                .product_id(applicationProperties.getProduct().getName())
                .channel("ticker")
                .channel("user")
                .build();

        final String subscribeJson = jsonMapper.writeValueAsString(subscriberequest);
        LOG.debug("Sig content {}", subscribeJson);

        final SignatureHeader signature = signatureService.getSignature("/users/self/verify", "GET", null);

        subscriberequest.setKey(signature.getCbAccessKey());
        subscriberequest.setSignature(signature.getCbAccessSign());
        subscriberequest.setTimestamp(signature.getCbAccessTimestamp());
        subscriberequest.setPassphrase(signature.getCbAccessPassphrase());

        final String subscribeMsg = jsonMapper.writeValueAsString(subscriberequest);
        LOG.debug("Sending subscribe request : {}", subscribeMsg);
        session.getBasicRemote().sendText(subscribeMsg);
    }

    @OnMessage
    public void processMessage(String message) {
        LOG.debug("GDAX FEED : receive message : {}", message);
        this.handleFeedMessageService.handleJsonMessage(message);
    }

    @OnClose
    public void processClose(Session session, CloseReason reason) throws IOException {
        LOG.debug("WebSocket has been close with reason " + reason.toString());
    }

    @OnError
    public void processError(Throwable t) {
        t.printStackTrace();
    }

}
