package com.wizaord.boursycrypto.gdax.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wizaord.boursycrypto.gdax.domain.E_FeedMessage;
import com.wizaord.boursycrypto.gdax.domain.GenericFeedMessage;
import com.wizaord.boursycrypto.gdax.domain.feedmessage.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class HandleFeedMessageService {

  private static final Logger LOG = LoggerFactory.getLogger(HandleFeedMessageService.class);

  @Autowired
  private ObjectMapper jsonMapper;

  /**
   * Cette fonction permet à partir d'un object JSON, de recupérer l'ordre recu par GDAX
   * L'ordre recu est alors envoyer à la fonction handleGdaxAction pour traitement
   *
   * @param message
   */
  public void handleMessage(String message) {
    GenericFeedMessage feedMessage = null;
    try {
      feedMessage = jsonMapper.readValue(message, GenericFeedMessage.class);
    }
    catch (IOException e) {
      LOG.error("Unable to parse the receive feedMessage", e);
    }

    final Optional<GenericFeedMessage> convertedMessage = this.convertGenericMessage(message, feedMessage.getType());

  }

  /**
   * Cette fonction convertie le parametre @feedMessage qui est au format JSON en un object JAVA en fonction du type de
   * message. Si le type n'est pas connu ou si la convertion remonte une erreur, un {@link Optional<GenericFeedMessage>} est retourné.
   * @param feedMessage : le message recu de GDAX
   * @param messageType : le type du message recu de GDAX
   * @return
   */
  private Optional<GenericFeedMessage> convertGenericMessage(final String feedMessage, final String messageType) {
    E_FeedMessage eFeedMessage;
    GenericFeedMessage convertedFeedMessage = null;
    try {
      eFeedMessage = E_FeedMessage.valueOf(messageType.toUpperCase());
    }
    catch (IllegalArgumentException e) {
      LOG.error("Unknow message type " + messageType);
      return Optional.empty();
    }

    try {
      switch (eFeedMessage) {
        case TICKER:
          convertedFeedMessage = jsonMapper.readValue(feedMessage, Ticker.class);
          break;
      }
    }
    catch (IOException e) {
      LOG.error("unable to convert " + feedMessage + " in internal Object", e);
    }

    return Optional.ofNullable(convertedFeedMessage);
  }

  public void handleGdaxAction(final GenericFeedMessage gdaxAction) {

  }
}
