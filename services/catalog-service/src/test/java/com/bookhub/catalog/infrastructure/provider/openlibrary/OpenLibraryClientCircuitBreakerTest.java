package com.bookhub.catalog.infrastructure.provider.openlibrary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.bookhub.catalog.application.error.ExternalServiceUnavailableException;
import com.bookhub.catalog.config.OpenLibraryProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OpenLibraryClientCircuitBreakerTest {

  private MockRestServiceServer mockServer;
  private OpenLibraryClient openLibraryClient;
  private CircuitBreaker circuitBreaker;

  @BeforeEach
  void setUp() {
    final RestClient.Builder restClientBuilder =
        RestClient.builder().baseUrl("https://openlibrary.org");
    mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();

    circuitBreaker =
        CircuitBreaker.of(
            "openLibrary",
            CircuitBreakerConfig.custom()
                .slidingWindowSize(2)
                .minimumNumberOfCalls(2)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(300))
                .permittedNumberOfCallsInHalfOpenState(1)
                .build());

    openLibraryClient =
        new OpenLibraryClient(
            restClientBuilder.build(),
            new OpenLibraryProperties(true, "https://openlibrary.org", 2000, null),
            new SimpleMeterRegistry(),
            circuitBreaker);
  }

  @Test
  void shouldShortCircuitWhenCircuitBreakerIsOpen() {
    mockServer
        .expect(requestTo("https://openlibrary.org/works/OL404W.json"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withServerError());
    mockServer
        .expect(requestTo("https://openlibrary.org/works/OL404W.json"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withServerError());

    assertThatThrownBy(() -> openLibraryClient.fetchDetail("OL404W"))
        .isInstanceOf(ExternalServiceUnavailableException.class);
    assertThatThrownBy(() -> openLibraryClient.fetchDetail("OL404W"))
        .isInstanceOf(ExternalServiceUnavailableException.class);

    assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

    assertThatThrownBy(() -> openLibraryClient.fetchDetail("OL404W"))
        .isInstanceOf(ExternalServiceUnavailableException.class);
  }

  @Test
  void shouldRecoverAfterHalfOpenProbeSucceeds() throws InterruptedException {
    mockServer
        .expect(requestTo("https://openlibrary.org/works/OL777W.json"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withServerError());
    mockServer
        .expect(requestTo("https://openlibrary.org/works/OL777W.json"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withServerError());
    mockServer
        .expect(requestTo("https://openlibrary.org/works/OL777W.json"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                """
                        {
                          "key": "/works/OL777W",
                          "title": "Recovery Book"
                        }
                        """,
                MediaType.APPLICATION_JSON));

    assertThatThrownBy(() -> openLibraryClient.fetchDetail("OL777W"))
        .isInstanceOf(ExternalServiceUnavailableException.class);
    assertThatThrownBy(() -> openLibraryClient.fetchDetail("OL777W"))
        .isInstanceOf(ExternalServiceUnavailableException.class);
    assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

    Thread.sleep(350);

    openLibraryClient.fetchDetail("OL777W");

    assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
  }
}
