package com.bookhub.library.config;

import io.micrometer.observation.ObservationRegistry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.springframework.boot.actuate.metrics.web.client.ObservationRestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
public class RestClientObservationConfig {

  @Bean
  public TextMapPropagator w3cTraceContextPropagator() {
    return W3CTraceContextPropagator.getInstance();
  }

  @Bean
  public RestClient.Builder restClientBuilder(final ObservationRegistry observationRegistry) {
    final RestClient.Builder restClientBuilder = RestClient.builder();
    new ObservationRestClientCustomizer(
            observationRegistry,
            new DefaultClientRequestObservationConvention("http.client.requests"))
        .customize(restClientBuilder);
    return restClientBuilder;
  }
}
