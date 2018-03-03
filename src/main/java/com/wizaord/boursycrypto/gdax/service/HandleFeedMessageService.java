package com.wizaord.boursycrypto.gdax.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wizaord.boursycrypto.gdax.domain.GenericFeedMessage;
import com.wizaord.boursycrypto.gdax.domain.feedmessage.E_FeedMessage;
import com.wizaord.boursycrypto.gdax.domain.feedmessage.Ticker;
import com.wizaord.boursycrypto.gdax.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class HandleFeedMessageService {

  private static final Logger LOG = LoggerFactory.getLogger(HandleFeedMessageService.class);

  @Autowired
  private ObjectMapper jsonMapper;
  @Autowired
  private TradeService tradeService;
  @Autowired
  private TendanceService tendanceService;
  @Autowired
  private JmsMessagingTemplate jmsTemplate;

  /**
   * Cette fonction permet à partir d'un object JSON, de recupérer l'ordre recu par GDAX
   * L'ordre recu est alors envoyer à la fonction handleGdaxAction pour traitement
   *
   * @param message
   */
  public void handleMessage(final String message) {
    try {
      final GenericFeedMessage feedMessage = jsonMapper.readValue(message, GenericFeedMessage.class);
      final Optional<E_FeedMessage> feedMessageType = E_FeedMessage.getByName(feedMessage.getType());
      if(feedMessageType.isPresent()) {
        final GenericFeedMessage mapperMessage = jsonMapper.readValue(message, feedMessageType.get().javaType);
        this.handleGdaxAction(mapperMessage);
      } else {
        LOG.warn("Unknow message type => " + message);
      }
    }
    catch (IOException e) {
      LOG.error("Unable to parse the receive feedMessage", e);
    }

  }

  public void handleGdaxAction(final GenericFeedMessage gdaxAction) {
    LOG.debug("Handle new message with type : " + gdaxAction.getType());
    if (gdaxAction instanceof Ticker) {
      this.handleTickerMessage((Ticker) gdaxAction);
    }
  }

  protected void handleTickerMessage(final Ticker tickerMessage) {
    jmsTemplate.convertAndSend("gdax.feed.ticker", "plop");
    tendanceService.notifyTickerMessage(tickerMessage);
    tradeService.notifyNewTickerMessage(tickerMessage);
  }
}
