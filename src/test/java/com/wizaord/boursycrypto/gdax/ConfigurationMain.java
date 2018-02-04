package com.wizaord.boursycrypto.gdax;

import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class ConfigurationMain {
}
