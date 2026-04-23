package com.bookhub.catalog.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OpenLibraryProperties.class)
public class CatalogSearchConfig {

  @Bean
  public RestClient openLibraryRestClient(final OpenLibraryProperties openLibraryProperties) {
    final Duration timeout = Duration.ofMillis(openLibraryProperties.timeoutMs());
    final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(timeout);
    requestFactory.setReadTimeout(timeout);
    return RestClient.builder()
        .baseUrl(openLibraryProperties.url())
        .requestFactory(requestFactory)
        .build();
  }

  @Bean
  public Duration providerTimeout(final OpenLibraryProperties openLibraryProperties) {
    return Duration.ofMillis(openLibraryProperties.timeoutMs());
  }

  @Bean
  public CircuitBreaker openLibraryCircuitBreaker(
      final OpenLibraryProperties openLibraryProperties) {
    final OpenLibraryProperties.CircuitBreaker breaker = openLibraryProperties.circuitBreaker();
    final CircuitBreakerConfig config =
        CircuitBreakerConfig.custom()
            .slidingWindowSize(breaker.slidingWindowSize())
            .minimumNumberOfCalls(breaker.minimumNumberOfCalls())
            .failureRateThreshold(breaker.failureRateThreshold())
            .waitDurationInOpenState(Duration.ofMillis(breaker.waitDurationOpenStateMs()))
            .permittedNumberOfCallsInHalfOpenState(breaker.permittedNumberOfCallsInHalfOpenState())
            .build();
    return CircuitBreaker.of(breaker.name(), config);
  }

  @Bean
  public Executor searchExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
