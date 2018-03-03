package com.wizaord.boursycrypto.gdax.domain.feedmessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wizaord.boursycrypto.gdax.ConfigurationMain;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConfigurationMain.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class OrderReceivedTest {


    @Autowired
    private ObjectMapper jsonMapper;

    @Test
    public void loadJsonMessage() throws IOException {
        File f = new File(getClass().getClassLoader().getResource("jsonFiles/feedMessages/01-newOrderPosted.json").getFile());
        assertThat(f).canRead();
        final OrderReceived orderMapped = jsonMapper.readValue(f, OrderReceived.class);

        assertThat(orderMapped.getOrderType()).isEqualToIgnoringCase("limit");

    }


}