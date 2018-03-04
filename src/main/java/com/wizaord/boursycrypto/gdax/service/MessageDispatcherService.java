package com.wizaord.boursycrypto.gdax.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wizaord.boursycrypto.gdax.domain.GenericFeedMessage;
import com.wizaord.boursycrypto.gdax.domain.feedmessage.*;
import com.wizaord.boursycrypto.gdax.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class MessageDispatcherService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageDispatcherService.class);

    @Autowired
    private ObjectMapper jsonMapper;
    @Autowired
    private TradeService tradeService;
    @Autowired
    private TendanceService tendanceService;

    /**
     * Cette fonction permet à partir d'un object JSON, de recupérer l'ordre recu par GDAX
     * L'ordre recu est alors envoyer à la fonction handleGdaxAction pour traitement
     *
     * @param message
     */
    public void handleJsonMessage(final String message) {
        try {
            final GenericFeedMessage feedMessage = jsonMapper.readValue(message, GenericFeedMessage.class);
            final Optional<E_FeedMessage> feedMessageType = E_FeedMessage.getByName(feedMessage.getType());
            if (feedMessageType.isPresent()) {
                final GenericFeedMessage mapperMessage = jsonMapper.readValue(message, feedMessageType.get().javaType);
                this.handleGdaxAction(mapperMessage);
            } else {
                LOG.warn("Unknow message type => " + message);
            }
        } catch (IOException e) {
            LOG.error("Unable to parse the receive feedMessage", e);
        }

    }

    /**
     * Dispatch the message based on its type
     *
     * @param gdaxAction
     */
    public void handleGdaxAction(final GenericFeedMessage gdaxAction) {
        LOG.debug("Handle new message with type : " + gdaxAction.getType());
        if (gdaxAction instanceof Ticker) {
            this.handleTickerMessage((Ticker) gdaxAction);
        } else if (gdaxAction instanceof OrderActivated) {
            handleOrderActivateMessage((OrderActivated) gdaxAction);
        } else if (gdaxAction instanceof OrderOpen) {
            handleOrderOpenMessage((OrderOpen) gdaxAction);
        } else if (gdaxAction instanceof OrderDone) {
            handleOrderDoneMessage((OrderDone) gdaxAction);
        } else if (gdaxAction instanceof OrderReceived) {
            LOG.info("Order received by GDAX");
        } else {
            LOG.error("Unable to handle message with type {}", gdaxAction.getType());
        }

    }

    protected void handleOrderDoneMessage(final OrderDone orderDoneMessage) {
        if (orderDoneMessage.getSide().equals("sell")) {
            if (orderDoneMessage.getReason().equals("canceled")) {
                // order has been canceled
                LOG.debug("Canceled order message has been received");
                this.tradeService.notifySellOrderCanceled(orderDoneMessage.getOrderId());
            } else {
                // order has been executed
                LOG.debug("Finish order message has been received");
                this.tradeService.notifySellOrderFinished(orderDoneMessage);
            }
        } else {
            // TODO : buy message
        }
    }

    /**
     * Handle orderOpenMessage
     */
    protected void handleOrderOpenMessage(final OrderOpen orderOpenMessage) {
        if (orderOpenMessage.getSide().equals("sell")) {
            this.tradeService.notifySellOrderOpen(orderOpenMessage);
        } else {
            // TODO : new buy message posted
        }

    }

    protected void handleOrderActivateMessage(final OrderActivated orderActivated) {
        if (orderActivated.getSide().equals("sell")) {
            this.tradeService.notifySellOrderActivated(orderActivated);
        } else {
            // TODO : new stop buy message posted
        }
    }

    /**
     * Handle Ticker Message
     *
     * @param tickerMessage
     */
    protected void handleTickerMessage(final Ticker tickerMessage) {
        tendanceService.notifyTickerMessage(tickerMessage);
        tradeService.notifyNewTickerMessage(tickerMessage);
    }
}
