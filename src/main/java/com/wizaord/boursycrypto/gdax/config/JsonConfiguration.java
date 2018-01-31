package com.wizaord.boursycrypto.gdax.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JsonConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(JsonConfiguration.class);

  @PostConstruct
  public void log() {
    LOG.info("JSON Configuration : Successfully loaded");
  }

  @Bean
  public ObjectMapper jsonMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    JavaTimeModule timeModule = new JavaTimeModule();
    timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));
    timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    objectMapper.registerModule(timeModule);
    return objectMapper;
  }

}
