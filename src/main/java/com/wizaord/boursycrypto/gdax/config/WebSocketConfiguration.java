package com.wizaord.boursycrypto.gdax.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;
import java.io.IOException;

@Configuration
public class WebSocketConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(WebSocketConfiguration.class);

  public static final String GDAX_WEBSOCKET = "wss://ws-feed.gdax.com";
  //  private final String GDAX_WEBSOCKET = "wss://demos.kaazing.com/echo";

  @PostConstruct
  public void log() {
    LOG.info("WebSocketConfiguration: Successfully loaded");
  }

  @Bean
  public WebSocketContainer getWebSocketContainer() throws IOException, DeploymentException {
      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      container.setDefaultMaxTextMessageBufferSize(999999);
      return container;
  }

}
