package com.wizaord.boursycrypto.gdax.service;

import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import com.wizaord.boursycrypto.gdax.domain.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
public class AccountService {

  private static final Logger LOG = LoggerFactory.getLogger(AccountService.class);

  @Autowired
  private RestTemplate restTemplate;
  @Autowired
  private ApplicationProperties appProp;

  private Float money;
  private Float btc;

  /**
   * Refresh the money and btc from the GDAX account
   */
  public void refreshBalance() {
    LOG.debug("Retrieving account balances..");
    final ResponseEntity<Account[]> accounts = restTemplate.getForEntity("/accounts", Account[].class);
    if (accounts.getStatusCode() != HttpStatus.OK) {
      LOG.error("Unable to get the user account");
    } else {
      Arrays.asList(accounts.getBody()).stream()
              .filter(account -> account.getCurrency().equals(appProp.getProduct().getType()))
              .findFirst()
              .ifPresent(account -> this.btc = account.getAvailable().floatValue());

      Arrays.asList(accounts.getBody()).stream()
              .filter(account -> account.getCurrency().equals(appProp.getProduct().getType()))
              .findFirst()
              .ifPresent(account -> this.money = account.getAvailable().floatValue());
    }

    this.logBalance();
  }

  public void logBalance() {
    LOG.info("----------------------------------------------------");
    LOG.info("Balance successfully loaded : ");
    LOG.info("   Money: {} €", this.money);
    LOG.info("   BTC:   {} BTC", this.btc);
    LOG.info("----------------------------------------------------");
  }

  public Float getMoney() {
    return money;
  }

  public Float getBtc() {
    return btc;
  }
}
