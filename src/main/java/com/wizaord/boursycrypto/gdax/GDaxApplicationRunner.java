package com.wizaord.boursycrypto.gdax;

import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import com.wizaord.boursycrypto.gdax.domain.api.Fill;
import com.wizaord.boursycrypto.gdax.domain.api.Order;
import com.wizaord.boursycrypto.gdax.domain.feedmessage.OrderActivated;
import com.wizaord.boursycrypto.gdax.listener.weksocket.FeedListener;
import com.wizaord.boursycrypto.gdax.service.AccountService;
import com.wizaord.boursycrypto.gdax.service.gdax.OrderService;
import com.wizaord.boursycrypto.gdax.service.notify.SlackService;
import com.wizaord.boursycrypto.gdax.service.trade.TradeService;
import com.wizaord.boursycrypto.gdax.service.trade.TradingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.wizaord.boursycrypto.gdax.domain.E_TradingMode.ACHAT;
import static com.wizaord.boursycrypto.gdax.domain.E_TradingMode.VENTE;

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
    @Autowired
    private TradingMode tradeMode;

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
        Optional<List<Order>> orders = orderService.loadSellOrders();
        boolean isOrderExixt = (orders.isPresent() && ! orders.get().isEmpty());
        if (this.accountService.getBtc() > 0 || isOrderExixt) {
            // notify order in the trade service
            if (isOrderExixt) {
                final Order firstOrder = orders.get().get(0);
                this.tradeService.notifySellOrderActivated(new OrderActivated(firstOrder));
            }
            // recuperation et injection de l'ordre d'achat
            final Fill lastFill = this.orderService.getLastBuyFill().get();
            this.tradeService.notifyBuyOrderPassed(lastFill);
            // mode vente
            this.tradeMode.setTraderMode(VENTE);
        } else {
            // mode achat
            this.tradeMode.setTraderMode(ACHAT);
        }
    }

    private void startWebSocket() {
        webSocketHandler.startConnection();
    }
}
