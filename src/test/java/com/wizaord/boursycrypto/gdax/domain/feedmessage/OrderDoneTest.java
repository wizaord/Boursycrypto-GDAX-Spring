package com.wizaord.boursycrypto.gdax.domain.feedmessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wizaord.boursycrypto.gdax.ConfigurationMain;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConfigurationMain.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class OrderDoneTest {

    @Autowired
    private ObjectMapper jsonMapper;

    @Test
    public void loadJsonMessage() throws IOException {
        File f = new File(getClass().getClassLoader().getResource("jsonFiles/feedMessages/03-newOrderCanceled.json").getFile());
        Assertions.assertThat(f).canRead();
        final OrderDone orderMapped = jsonMapper.readValue(f, OrderDone.class);

        Assertions.assertThat(orderMapped.getRemainingSize()).isEqualTo(0.42533266F);

    }


}