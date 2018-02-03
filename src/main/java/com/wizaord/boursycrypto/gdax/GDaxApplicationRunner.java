package com.wizaord.boursycrypto.gdax;

import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import com.wizaord.boursycrypto.gdax.listener.FeedListener;
import com.wizaord.boursycrypto.gdax.service.AccountService;
import com.wizaord.boursycrypto.gdax.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;

import static com.wizaord.boursycrypto.gdax.config.WebSocketConfiguration.GDAX_WEBSOCKET;

@Component
public class GDaxApplicationRunner implements ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(GDaxApplicationRunner.class);

  @Autowired
  private AccountService accountService;
  @Autowired
  private ApplicationProperties applicationProperties;
  @Autowired
  private OrderService orderService;
  @Autowired
  private WebSocketContainer webSocketContainer;
  @Autowired
  private FeedListener feedListener;

  @Override
  public void run(final ApplicationArguments args) {
    LOG.info("Starting GDaxApplication Runner !!!!");
    if (applicationProperties.getTrader().getVente().getStart().getCleanCurrentOrder()) {
      removeLastCurrentOrder();
    }
    refreshAccount();
    startWebSocket();
  }

  private void refreshAccount() {
    // loading account
    accountService.refreshBalance();
  }

  private void removeLastCurrentOrder() {
    //load last orders
    orderService.cancelOrders();
  }

  private void startWebSocket() {
    // start feed
    LOG.info("Connecting WebSocket to URL : {}", GDAX_WEBSOCKET);
    try {
      webSocketContainer.connectToServer(feedListener, URI.create(GDAX_WEBSOCKET));
    }
    catch (DeploymentException | IOException e) {
      LOG.error("Unable to start the webSocket", e);
    }
  }
}
