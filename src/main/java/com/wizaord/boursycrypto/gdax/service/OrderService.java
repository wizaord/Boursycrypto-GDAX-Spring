package com.wizaord.boursycrypto.gdax.service;

import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import com.wizaord.boursycrypto.gdax.domain.api.Fill;
import com.wizaord.boursycrypto.gdax.domain.api.Order;
import com.wizaord.boursycrypto.gdax.domain.api.PlaceOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {
  private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);

  @Autowired
  private RestTemplate restTemplate;
  @Autowired
  private ApplicationProperties applicationProperties;

  public Optional<List<Order>> loadOrders() {
    LOG.debug("Retrieving orders..");
    final ResponseEntity<Order[]> orders = restTemplate.getForEntity("/orders", Order[].class);
    if (orders.getStatusCode() != HttpStatus.OK) {
      LOG.error("Unable to get the orders");
      return Optional.empty();
    } else {
      return Optional.of(Arrays.asList(orders.getBody())
              .stream()
              .filter(o -> o.getProduct_id().equals(this.applicationProperties.getProduct().getName()))
              .collect(Collectors.toList()));
    }
  }

  public Optional<List<Fill>> loadFills() {
    LOG.debug("Retrieving fills..");
    final ResponseEntity<Fill[]> fills = restTemplate.getForEntity("/fills", Fill[].class);
    if (fills.getStatusCode() != HttpStatus.OK) {
      LOG.error("Unable to get the orders");
      return Optional.empty();
    } else {
      return Optional.of(Arrays.asList(fills.getBody())
              .stream()
              .filter(f -> f.getProduct_id().equals(this.applicationProperties.getProduct().getName()))
              .collect(Collectors.toList()));
    }
  }

  public Optional<Fill> getLastBuyFill() {
    final Optional<List<Fill>> fills = this.loadFills();
    if (fills.isPresent()) {
      return fills.get().stream()
              .sorted((o1, o2) -> (int) ((o1.getTrade_id() - o2.getTrade_id()) * -1))
              .findFirst();
    }
    return Optional.empty();
  }

  public void cancelOrder(final String orderId) {
    LOG.debug("Cancel order with ID {}", orderId);
    restTemplate.delete("/orders/" + orderId);
  }

  public Optional<Order> placeStopSellOrder(final double priceP, final double nbCoin) {
    LOG.info("Place a STOP ORDER TO {}", priceP);
    final PlaceOrder placeOrder = PlaceOrder.builder()
            .productId(this.applicationProperties.getProduct().getName())
            .side("sell")
//            .type("market")
            .size("0.1")
            .price("800")
//            .stop("loss")
//            .stopPrice(String.valueOf(priceP))
            .build();

    LOG.info("Positionnement d'un StopOrder a {} pour {}", priceP, nbCoin);
//    SlackService._instance.postMessage('positionnement d un stopOrder a ' + priceP + ' pour ' + nbCoin + ' coins');

    final ResponseEntity<Order> placeOrderResponse = restTemplate.postForEntity("/orders", placeOrder, Order.class);
    if (placeOrderResponse.getStatusCode() != HttpStatus.OK) {
      LOG.error("Unable to place the orders : {}", placeOrderResponse.toString());
      return Optional.empty();
    } else {
      return Optional.of(placeOrderResponse.getBody());
    }
  }
}
