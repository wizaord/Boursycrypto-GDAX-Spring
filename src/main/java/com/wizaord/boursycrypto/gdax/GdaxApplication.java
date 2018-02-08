package com.wizaord.boursycrypto.gdax;

import com.wizaord.boursycrypto.gdax.config.properties.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication //@EnableAutoConfiguration , @ComponentScan, SpringBootConfiguration
@EnableConfigurationProperties(ApplicationProperties.class)
@EnableScheduling
public class GdaxApplication {
  private static final Logger LOG = LoggerFactory.getLogger(GdaxApplication.class);

  private final Environment env;

  public GdaxApplication(Environment env) {
    this.env = env;
  }

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(GdaxApplication.class);
    ConfigurableApplicationContext context = app.run(args);

    Environment env = context.getEnvironment();
    LOG.info("\n----------------------------------------------------------\n\t" +
                    "Application '{}' is running!\n\t" +
                    "Profile(s): \t{}\n----------------------------------------------------------",
            env.getProperty("spring.application.name"),
            env.getActiveProfiles());
  }
}
