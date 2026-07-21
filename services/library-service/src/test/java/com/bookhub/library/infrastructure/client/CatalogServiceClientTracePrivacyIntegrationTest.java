package com.bookhub.library.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.bookhub.library.support.PostgreSqlIntegrationTest;
import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(TracePrivacyTestConfiguration.class)
class CatalogServiceClientTracePrivacyIntegrationTest extends PostgreSqlIntegrationTest {

  private static final String SENSITIVE_SERVICE_TOKEN = "sensitive-service-token-should-not-leak";
  private static final AtomicReference<String> RECEIVED_AUTHORIZATION = new AtomicReference<>();
  private static final HttpServer CATALOG_STUB = startCatalogStub();

  @Autowired private CatalogServiceClient catalogServiceClient;
  @Autowired private TracePrivacySpanExporter tracePrivacySpanExporter;
  @Autowired private OpenTelemetry openTelemetry;
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

  @ParameterizedTest
  @ValueSource(
      strings = {SENSITIVE_SERVICE_TOKEN, "another-sensitive-service-token-should-not-leak"})
  void shouldExcludeSensitiveServiceTokenFromExportedSpansAndLogMetadata(
      final String sensitiveServiceToken) {
    when(serviceTokenProvider.getServiceToken()).thenReturn(sensitiveServiceToken);
    final Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    final ListAppender<ILoggingEvent> logAppender = new ListAppender<>();
    rootLogger.addAppender(logAppender);
    logAppender.start();

    try {
      catalogServiceClient.findBookById(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    } finally {
      logAppender.stop();
      rootLogger.detachAppender(logAppender);
    }

    assertThat(openTelemetry).isInstanceOf(OpenTelemetrySdk.class);
    ((OpenTelemetrySdk) openTelemetry)
        .getSdkTracerProvider()
        .forceFlush()
        .join(5, TimeUnit.SECONDS);

    assertThat(RECEIVED_AUTHORIZATION.get()).isEqualTo("Bearer " + sensitiveServiceToken);
    assertThat(tracePrivacySpanExporter.exportedSpans()).isNotEmpty();
    assertThat(tracePrivacySpanExporter.exportedSpanAttributes())
        .doesNotContain(sensitiveServiceToken);
    assertThat(logData(logAppender.list)).doesNotContain(sensitiveServiceToken);
  }

  private List<String> logData(final List<ILoggingEvent> loggingEvents) {
    return loggingEvents.stream()
        .flatMap(
            event ->
                java.util.stream.Stream.concat(
                    java.util.stream.Stream.of(event.getFormattedMessage()),
                    event.getMDCPropertyMap().entrySet().stream()
                        .flatMap(
                            entry -> java.util.stream.Stream.of(entry.getKey(), entry.getValue()))))
        .toList();
  }

  private static HttpServer startCatalogStub() {
    try {
      final HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
      server.createContext(
          "/api/v1/internal/books/",
          exchange -> {
            RECEIVED_AUTHORIZATION.set(exchange.getRequestHeaders().getFirst("Authorization"));
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
      throw new IllegalStateException("Unable to start catalog privacy test stub", exception);
    }
  }
}
