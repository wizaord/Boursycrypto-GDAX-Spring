package com.wizaord.boursycrypto.gdax.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;

@Configuration
public class WebSocketConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketConfiguration.class);
    public static String GDAX_WEBSOCKET;

    @PostConstruct
    public void log() {
        LOG.info("WebSocketConfiguration: Successfully loaded. Url {}", GDAX_WEBSOCKET);
    }


    @Value("${application.configuration.feedurl}")
    public void setWebSocketUrl(String webSocketUrl) {
        GDAX_WEBSOCKET = webSocketUrl;
    }

    @Bean
    public WebSocketContainer gdaxWebSocketContainer() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.setDefaultMaxTextMessageBufferSize(9999999);
        container.setDefaultMaxSessionIdleTimeout(0);
        return container;
    }

}
