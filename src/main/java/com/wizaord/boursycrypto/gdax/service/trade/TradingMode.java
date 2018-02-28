package com.wizaord.boursycrypto.gdax.service.trade;

import com.wizaord.boursycrypto.gdax.domain.E_TradingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TradingMode {

    private static final Logger LOG = LoggerFactory.getLogger(TradingMode.class);

    /**
     * Le mode de fonctionnement de l'application
     */
    private E_TradingMode traderMode = E_TradingMode.NOORDER;

    /**
     * Getter pour récupérer le mode de transaction
     * @return
     */
    public E_TradingMode getTraderMode() {
        return traderMode;
    }

    /**
     * Fonction permettant de changer le tradingMode
     * @param mode
     */
    public void setTraderMode(final E_TradingMode mode) {
        LOG.info("New trading mode {}", mode);
        this.traderMode = mode;
    }
}
