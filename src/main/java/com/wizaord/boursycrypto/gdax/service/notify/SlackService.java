package com.wizaord.boursycrypto.gdax.service.notify;

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
    this.sendMessage(message, this.appProp.getSlack().getPersonalAccountChannel());
  }

  /**
   * Send a message to the complete list of users
   * @param message
   */
  public void postListChannel(final String message) {
      this.appProp.getSlack().getListChannel().forEach(x -> this.sendMessage(message, x));
  }

  /**
   * Send a message to a specific channel
   * @param message
   * @param channel
   */
  public void sendMessage(final String message, final String channel) {
    LOG.debug("Sending message to slack : {}", message);
    this.sendSlackMessage(SLACK_ACTION, channel , message);
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
