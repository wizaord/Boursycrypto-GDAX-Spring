package com.wizaord.boursycrypto.gdax;

import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import com.wizaord.boursycrypto.gdax.listener.weksocket.FeedListener;
import com.wizaord.boursycrypto.gdax.service.AccountService;
import com.wizaord.boursycrypto.gdax.service.gdax.OrderService;
import com.wizaord.boursycrypto.gdax.service.notify.SlackService;
import com.wizaord.boursycrypto.gdax.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"PROD", "SANDBOX"})
public class GDaxApplicationRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(GDaxApplicationRunner.class);

    @Autowired
    private SlackService slackService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private OrderService orderService;
    @Autowired
    private FeedListener webSocketHandler;
    @Autowired
    private TradeService tradeService;

    @Override
    public void run(final ApplicationArguments args) {
        LOG.info("Starting GDaxApplication Runner !!!!");
        slackService.postCustomMessage("Starting GDaxApplication for " + this.applicationProperties.getProduct().getName());

        //refresh account
        refreshAccount();

        // clean current order if option is activated
        if (applicationProperties.getTrader().getVente().getStart().getCleanCurrentOrder()) {
            removeLastCurrentOrder();
        }

        // if order is placed => sell MODE
        // if BTC exists => sell MODE
        // else => BUY MODE
        initTradeMode();

        // starting the webSocket
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

    /**
     *
     // if order is placed => sell MODE
     // if BTC exists => sell MODE
     // else => BUY MODE
     */
    private void initTradeMode() {
        this.tradeService.determineTradeMode();
    }

    private void startWebSocket() {
        webSocketHandler.startConnection();
    }
}
