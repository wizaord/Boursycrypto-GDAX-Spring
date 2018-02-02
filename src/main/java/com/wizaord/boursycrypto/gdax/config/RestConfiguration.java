package com.wizaord.boursycrypto.gdax.config;

import com.wizaord.boursycrypto.gdax.interceptor.GDaxAuthInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfiguration {

  public static final String GDAX_URI = "https://api.gdax.com";

  @Bean
  public ClientHttpRequestInterceptor gdaxAuthInterceptor() {
    return new GDaxAuthInterceptor();
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder, ClientHttpRequestInterceptor gdaxAuthInterceptor) {
    return builder
            .rootUri(GDAX_URI)
            .additionalInterceptors(gdaxAuthInterceptor)
            .build();
  }

}
