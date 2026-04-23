package com.bookhub.catalog.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

class OpenLibraryPropertiesTest {

  @Test
  void shouldBindCircuitBreakerPropertiesFromConfiguration() {
    final ConfigurationPropertySource source =
        new MapConfigurationPropertySource(
            Map.of(
                "catalog.providers.openlibrary.enabled", "true",
                "catalog.providers.openlibrary.url", "https://openlibrary.org",
                "catalog.providers.openlibrary.timeout-ms", "2000",
                "catalog.providers.openlibrary.circuit-breaker.name", "openLibrary",
                "catalog.providers.openlibrary.circuit-breaker.sliding-window-size", "12",
                "catalog.providers.openlibrary.circuit-breaker.minimum-number-of-calls", "6",
                "catalog.providers.openlibrary.circuit-breaker.failure-rate-threshold", "60",
                "catalog.providers.openlibrary.circuit-breaker.wait-duration-open-state-ms",
                    "12000",
                "catalog.providers.openlibrary.circuit-breaker.permitted-number-of-calls-in-half-open-state",
                    "3"));

    final OpenLibraryProperties properties =
        new Binder(source)
            .bind("catalog.providers.openlibrary", OpenLibraryProperties.class)
            .orElseThrow(() -> new IllegalStateException("OpenLibrary properties should bind"));

    assertThat(properties.circuitBreaker().name()).isEqualTo("openLibrary");
    assertThat(properties.circuitBreaker().slidingWindowSize()).isEqualTo(12);
    assertThat(properties.circuitBreaker().minimumNumberOfCalls()).isEqualTo(6);
    assertThat(properties.circuitBreaker().failureRateThreshold()).isEqualTo(60f);
    assertThat(properties.circuitBreaker().waitDurationOpenStateMs()).isEqualTo(12_000L);
    assertThat(properties.circuitBreaker().permittedNumberOfCallsInHalfOpenState()).isEqualTo(3);
  }

  @Test
  void shouldApplyCircuitBreakerDefaultsWhenNotConfigured() {
    final OpenLibraryProperties properties =
        new OpenLibraryProperties(true, "https://openlibrary.org", 2000, null);

    assertThat(properties.circuitBreaker().name()).isEqualTo("openLibrary");
    assertThat(properties.circuitBreaker().slidingWindowSize()).isEqualTo(10);
    assertThat(properties.circuitBreaker().minimumNumberOfCalls()).isEqualTo(5);
    assertThat(properties.circuitBreaker().failureRateThreshold()).isEqualTo(50f);
    assertThat(properties.circuitBreaker().waitDurationOpenStateMs()).isEqualTo(30_000L);
    assertThat(properties.circuitBreaker().permittedNumberOfCallsInHalfOpenState()).isEqualTo(2);
  }
}
