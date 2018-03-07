package com.wizaord.boursycrypto.gdax.listener.weksocket;

import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;

public class ReconnectHandler extends ClientManager.ReconnectHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ReconnectHandler.class);

    @Override
    public boolean onDisconnect(CloseReason closeReason) {
        LOG.debug("WebSocket has been close. Launching reconnect");
        return true;
    }

    @Override
    public boolean onConnectFailure(Exception exception) {
        LOG.warn("WebSocket connection fails. Reconnecting");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

}
