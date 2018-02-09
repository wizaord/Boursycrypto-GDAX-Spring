package com.wizaord.boursycrypto.gdax.service;

import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SlackService {

  public static final Logger LOG = LoggerFactory.getLogger(SlackService.class);

  private static final String SLACK_API = "https://slack.com/api/";
  private static final String SLACK_ACTION = "chat.postMessage";

  @Autowired
  private ApplicationProperties appProp;


  @Autowired
  private RestTemplate restTemplate;

  /**
   * Send a message to the configured Slack channel
   * @param message
   */
  public void postCustomMessage(final String message) {
    LOG.debug("Sending message to slack : {}", message);
    this.sendSlackMessage("chat.postMessage", this.appProp.getSlack().getPersonalAccountChannel(), message);
  }

  private void sendSlackMessage(final String uri, final String channel, final String message) {
    final String msgWithPrefixe = this.appProp.getProduct().getType() + " - " + message;
    final String fullUri = uri + "?token=" + this.appProp.getSlack().getTokenId() + "&channel=" + channel + "&text=" + msgWithPrefixe + "&pretty=1";
    LOG.debug("Call REST SLACK API : " + fullUri);

    final ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(SlackService.SLACK_API + fullUri, null, String.class);
    if (stringResponseEntity.getStatusCode() != HttpStatus.OK) {
      LOG.error("Unable to post in Slack !!! Reason {}", stringResponseEntity.getBody());
    }
  }
}
