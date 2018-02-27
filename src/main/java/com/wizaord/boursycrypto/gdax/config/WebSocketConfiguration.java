package com.wizaord.boursycrypto.gdax.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;

@Configuration
public class WebSocketConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(WebSocketConfiguration.class);

  public static final String GDAX_WEBSOCKET = "wss://ws-feed.gdax.com";

  @PostConstruct
  public void log() {
    LOG.info("WebSocketConfiguration: Successfully loaded");
  }

  @Bean
  public WebSocketContainer getWebSocketContainer() {
      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      container.setDefaultMaxTextMessageBufferSize(9999999);
      container.setDefaultMaxSessionIdleTimeout(0);
      return container;
  }

}
