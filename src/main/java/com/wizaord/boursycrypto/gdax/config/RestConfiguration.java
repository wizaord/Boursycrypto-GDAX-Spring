package com.wizaord.boursycrypto.gdax.config;

import com.wizaord.boursycrypto.gdax.interceptor.GDaxAuthInterceptor;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RestConfiguration {

  public static final String GDAX_URI = "https://api.gdax.com";

  @Bean
  public ClientHttpRequestInterceptor gdaxAuthInterceptor() {
    return new GDaxAuthInterceptor();
  }

  @Bean
  public RestTemplate restTemplate(ClientHttpRequestInterceptor gdaxAuthInterceptor) {
    RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
    RootUriTemplateHandler.addTo(restTemplate, GDAX_URI);
    restTemplate.getInterceptors().add(gdaxAuthInterceptor);
    return restTemplate;
  }

  @Bean
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    return loggingFilter;
  }

}
