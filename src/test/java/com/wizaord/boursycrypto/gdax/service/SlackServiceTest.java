package com.wizaord.boursycrypto.gdax.service;

import com.wizaord.boursycrypto.gdax.ConfigurationMain;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConfigurationMain.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SlackServiceTest {

  @Autowired
  private SlackService slackService;

  @Test
  public void testSendMessage() {
    slackService.postCustomMessage("Hello");
  }

}