package com.wizaord.boursycrypto.gdax;

import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import com.wizaord.boursycrypto.gdax.listener.FeedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;

import static com.wizaord.boursycrypto.gdax.config.WebSocketConfiguration.GDAX_WEBSOCKET;

@SpringBootApplication //@EnableAutoConfiguration , @ComponentScan, SpringBootConfiguration
@EnableConfigurationProperties(ApplicationProperties.class)
public class GdaxApplication {
  private static final Logger LOG = LoggerFactory.getLogger(GdaxApplication.class);

  private final Environment env;

  public GdaxApplication(Environment env) {
    this.env = env;
  }

  public static void main(String[] args) throws IOException, DeploymentException {
    SpringApplication app = new SpringApplication(GdaxApplication.class);
    ConfigurableApplicationContext context = app.run(args);

    Environment env = context.getEnvironment();
    LOG.info("\n----------------------------------------------------------\n\t" +
                    "Application '{}' is running!\n\t" +
                    "Profile(s): \t{}\n----------------------------------------------------------",
            env.getProperty("spring.application.name"),
            env.getActiveProfiles());

    // start feed
    final WebSocketContainer webSocketContainer = context.getBean(WebSocketContainer.class);
    final FeedListener gDaxWebSocketService = context.getBean(FeedListener.class);
    LOG.info("Connecting WebSocket to URL : {}", GDAX_WEBSOCKET);
    webSocketContainer.connectToServer(gDaxWebSocketService, URI.create(GDAX_WEBSOCKET));
  }
}
