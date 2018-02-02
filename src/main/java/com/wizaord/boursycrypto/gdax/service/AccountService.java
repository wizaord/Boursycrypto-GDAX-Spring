package com.wizaord.boursycrypto.gdax.service;

import com.wizaord.boursycrypto.gdax.domain.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AccountService {

  private static final Logger LOG = LoggerFactory.getLogger(AccountService.class);

  @Autowired
  private RestTemplate restTemplate;

  private Float money;
  private Float btc;

  /**
   * Refresh the money and btc from the GDAX account
   */
  @Scheduled(fixedRate = 5000)
  public void refreshBalance() {
    LOG.debug("Retrieving account balances..");
    final ResponseEntity<Account[]> accounts = restTemplate.getForEntity("/accounts", Account[].class);
    LOG.info("Receive response {}", accounts);
    //    return Promise.resolve(this.gdaxExchangeApi.loadBalances().then((balances: Balances) => {
//      for (const profile in balances) {
//                const account = balances[profile];
//        this._money = Number(account.EUR.available);
//        this._btc = Number(account[this.confService.configurationFile.application.product.type].available);
//      }
//      this.logBalance();
//      return true;
//    })).catch((reason) => {
//            this.options.logger.log('error', 'Error while get the account balance');
//    this.options.logger.error(reason);
//    logError(reason);
//    return Promise.reject(reason);
//        });
  }

  public Float getMoney() {
    return money;
  }

  public Float getBtc() {
    return btc;
  }
}
