package com.bookhub.library.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.bookhub.library.support.PostgreSqlIntegrationTest;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class CatalogServiceClientTracePropagationIntegrationTest extends PostgreSqlIntegrationTest {

  private static final AtomicReference<String> RECEIVED_TRACEPARENT = new AtomicReference<>();
  private static final HttpServer CATALOG_STUB = startCatalogStub();

  @Autowired private CatalogServiceClient catalogServiceClient;
  @Autowired private Tracer tracer;
  @Autowired private ObservationRegistry observationRegistry;
  @MockitoBean private ServiceTokenProvider serviceTokenProvider;

  @DynamicPropertySource
  static void configureCatalogService(final DynamicPropertyRegistry registry) {
    registry.add(
        "catalog.service.base-url",
        () -> "http://localhost:" + CATALOG_STUB.getAddress().getPort());
  }

  @AfterAll
  static void stopCatalogStub() {
    CATALOG_STUB.stop(0);
  }

  @Test
  void shouldPropagateCurrentTraceContextToCatalogService() {
    when(serviceTokenProvider.getServiceToken()).thenReturn("service-token");
    final Observation observation =
        Observation.start("library.catalog.lookup", observationRegistry);

    try (Observation.Scope ignored = observation.openScope()) {
      final Span span = tracer.currentSpan();
      catalogServiceClient.findBookById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

      assertThat(RECEIVED_TRACEPARENT.get())
          .startsWith("00-" + span.context().traceId() + "-")
          .endsWith("-01")
          .hasSize(55);
    } finally {
      observation.stop();
    }
  }

  private static HttpServer startCatalogStub() {
    try {
      final HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
      server.createContext(
          "/api/v1/internal/books/",
          exchange -> {
            RECEIVED_TRACEPARENT.set(exchange.getRequestHeaders().getFirst("traceparent"));
            final byte[] response =
                """
                {"bookId":"00000000-0000-0000-0000-000000000001","title":"Traceable Book","pageCount":300}
                """
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
          });
      server.start();
      return server;
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to start catalog trace test stub", exception);
    }
  }
}
