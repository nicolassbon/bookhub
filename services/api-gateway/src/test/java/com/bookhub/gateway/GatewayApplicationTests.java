package com.bookhub.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.springframework.http.HttpHeaders.ORIGIN;

import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GatewayApplicationTests {

  private static int downstreamPort;
  private static DisposableServer downstreamStubServer = startDownstreamStubServer(0);

  @DynamicPropertySource
  static void registerGatewayRoutes(final DynamicPropertyRegistry registry) {
    final String downstreamBaseUrl = "http://localhost:" + downstreamPort;
    registry.add("IDENTITY_SERVICE_URL", () -> downstreamBaseUrl);
    registry.add("CATALOG_SERVICE_URL", () -> downstreamBaseUrl);
    registry.add("LIBRARY_SERVICE_URL", () -> downstreamBaseUrl);
    registry.add("spring.cloud.gateway.server.webflux.trusted-proxies", () -> ".*");
  }

  @AfterAll
  static void stopStubServer() {
    stopDownstreamStubServer();
  }

  @Autowired private RouteDefinitionLocator routeDefinitionLocator;

  @Autowired private WebTestClient webTestClient;

  @LocalServerPort private int serverPort;

  @Test
  void contextLoads() {}

  @Test
  void shouldConfigureRoutesForAllServices() {
    final List<RouteDefinition> routeDefinitions =
        routeDefinitionLocator.getRouteDefinitions().collectList().block();

    assertThat(routeDefinitions).isNotNull();
    assertThat(routeDefinitions)
        .extracting(RouteDefinition::getId)
        .containsExactlyInAnyOrder(
            "identity-auth-route",
            "identity-users-route",
            "identity-admin-route",
            "catalog-books-route",
            "catalog-admin-route",
            "library-route",
            "library-goals-route",
            "library-reviews-route",
            "library-notifications-route",
            "library-admin-route");

    assertThat(routeDefinitions)
        .anyMatch(
            routeDefinition ->
                routeDefinition.getId().equals("identity-admin-route")
                    && routeDefinition.getPredicates().stream()
                        .anyMatch(
                            predicateDefinition ->
                                predicateDefinition.toString().contains("/api/v1/admin/users/**")));

    assertThat(routeDefinitions)
        .anyMatch(
            routeDefinition ->
                routeDefinition.getId().equals("catalog-admin-route")
                    && routeDefinition.getPredicates().stream()
                        .anyMatch(
                            predicateDefinition ->
                                predicateDefinition.toString().contains("/api/v1/admin/books/**")));

    assertThat(routeDefinitions)
        .anyMatch(
            routeDefinition ->
                routeDefinition.getId().equals("library-admin-route")
                    && routeDefinition.getPredicates().stream()
                        .anyMatch(
                            predicateDefinition ->
                                predicateDefinition.toString().contains("/api/v1/admin/reviews/**")
                                    || predicateDefinition
                                        .toString()
                                        .contains("/api/v1/admin/metrics/**")));

    assertThat(routeDefinitions)
        .anyMatch(
            routeDefinition ->
                routeDefinition.getId().equals("identity-auth-route")
                    && routeDefinition.getPredicates().stream()
                        .anyMatch(
                            predicateDefinition ->
                                predicateDefinition.toString().contains("/api/v1/auth/**")));

    assertThat(routeDefinitions)
        .anyMatch(
            routeDefinition ->
                routeDefinition.getId().equals("identity-users-route")
                    && routeDefinition.getPredicates().stream()
                        .anyMatch(
                            predicateDefinition ->
                                predicateDefinition.toString().contains("/api/v1/users/**")));

    assertThat(routeDefinitions)
        .anyMatch(
            routeDefinition ->
                routeDefinition.getId().equals("catalog-books-route")
                    && routeDefinition.getPredicates().stream()
                        .anyMatch(
                            predicateDefinition ->
                                predicateDefinition.toString().contains("/api/v1/books/**")));
  }

  @Test
  void shouldReturnCorsHeadersForAllowedOriginPreflight() {
    webTestClient
        .method(HttpMethod.OPTIONS)
        .uri("http://localhost:" + serverPort + "/api/v1/auth/login")
        .header(ORIGIN, "https://bookhub.app")
        .header("Access-Control-Request-Method", "POST")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .valueEquals(ACCESS_CONTROL_ALLOW_ORIGIN, "https://bookhub.app")
        .expectHeader()
        .valueEquals(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
        .expectHeader()
        .value(
            ACCESS_CONTROL_ALLOW_METHODS,
            allowMethods -> assertThat(allowMethods).contains("POST"));
  }

  @Test
  void shouldRejectCorsPreflightForDisallowedOrigin() {
    webTestClient
        .method(HttpMethod.OPTIONS)
        .uri("http://localhost:" + serverPort + "/api/v1/auth/login")
        .header(ORIGIN, "http://malicious.local")
        .header("Access-Control-Request-Method", "POST")
        .exchange()
        .expectStatus()
        .isForbidden()
        .expectHeader()
        .doesNotExist(ACCESS_CONTROL_ALLOW_ORIGIN)
        .expectHeader()
        .doesNotExist(ACCESS_CONTROL_ALLOW_CREDENTIALS);
  }

  @Test
  void shouldReturnNotFoundForUnmatchedRoute() {
    webTestClient
        .get()
        .uri("http://localhost:" + serverPort + "/api/v1/unmapped")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void shouldKeepSingleEdgeCorsHeadersWhenDownstreamAlsoReturnsCorsHeaders() {
    webTestClient
        .get()
        .uri("http://localhost:" + serverPort + "/api/v1/books/cors-probe")
        .header(ORIGIN, "https://bookhub.app")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .value(
            ACCESS_CONTROL_ALLOW_ORIGIN,
            values -> assertThat(values).isEqualTo("https://bookhub.app"))
        .expectHeader()
        .value(ACCESS_CONTROL_ALLOW_CREDENTIALS, values -> assertThat(values).isEqualTo("true"));
  }

  @Test
  void shouldForwardAndPreservePathForAllRequiredRoutePrefixes() {
    assertDownstreamPathPreserved("/api/v1/auth/login?probe=auth");
    assertDownstreamPathPreserved("/api/v1/users/profile?probe=users");
    assertDownstreamPathPreserved("/api/v1/books/42?probe=books");
  }

  @Test
  void shouldPropagateUnauthorizedFromIdentityUpstream() {
    webTestClient
        .get()
        .uri("http://localhost:" + serverPort + "/api/v1/users/profile?probe=unauthorized")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void shouldPropagateForbiddenFromIdentityUpstream() {
    webTestClient
        .get()
        .uri("http://localhost:" + serverPort + "/api/v1/users/profile?probe=forbidden")
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void shouldReturn5xxWhenCatalogUpstreamReturns500() {
    webTestClient
        .get()
        .uri("http://localhost:" + serverPort + "/api/v1/books/42?probe=upstream-500")
        .exchange()
        .expectStatus()
        .is5xxServerError();
  }

  @Test
  void shouldReturn5xxWhenCatalogUpstreamIsUnavailable() {
    webTestClient
        .get()
        .uri("http://localhost:" + serverPort + "/api/v1/books/42?probe=unreachable")
        .exchange()
        .expectStatus()
        .is5xxServerError();
  }

  @Test
  void shouldForwardProtoAndHostHeadersToDownstreamService() {
    webTestClient
        .get()
        .uri("http://localhost:" + serverPort + "/api/v1/auth/forwarded-probe")
        .header(ORIGIN, "https://bookhub.app")
        .header("Forwarded", "proto=https;host=api.bookhub.local")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .value(
            "X-Downstream-Forwarded-Proto",
            forwardedProto -> assertThat(forwardedProto).contains("https"))
        .expectHeader()
        .value(
            "X-Downstream-Forwarded-Host",
            forwardedHost -> assertThat(forwardedHost).contains("api.bookhub.local"))
        .expectHeader()
        .value(
            "X-Downstream-Forwarded", forwarded -> assertThat(forwarded).contains("proto=https"));
  }

  @Test
  void shouldForwardValidInboundW3cTraceContextToDownstreamService() {
    final String inboundTraceparent = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";

    webTestClient
        .get()
        .uri("http://localhost:" + serverPort + "/api/v1/books/trace-probe")
        .header("traceparent", inboundTraceparent)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .valueEquals("X-Downstream-Traceparent", inboundTraceparent);
  }

  @Test
  void shouldPreserveInboundW3cTraceIdWhenForwardingToDownstreamService() {
    final String inboundTraceparent = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";

    webTestClient
        .get()
        .uri("http://localhost:" + serverPort + "/api/v1/books/trace-probe")
        .header("traceparent", inboundTraceparent)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .value(
            "X-Downstream-Traceparent",
            traceparent -> assertThat(traceparent).isEqualTo(inboundTraceparent));
  }

  @Test
  void shouldExposeMetricsButRequireAuthenticationForIt() {
    webTestClient
        .get()
        .uri("http://localhost:" + serverPort + "/actuator/metrics")
        .exchange()
        .expectStatus()
        .isUnauthorized();

    webTestClient
        .get()
        .uri("http://localhost:" + serverPort + "/actuator/prometheus")
        .exchange()
        .expectStatus()
        .isOk();
  }

  private void assertDownstreamPathPreserved(final String expectedPath) {
    webTestClient
        .get()
        .uri("http://localhost:" + serverPort + expectedPath)
        .header(ORIGIN, "https://bookhub.app")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .valueEquals("X-Downstream-Path", expectedPath);
  }

  private static DisposableServer startDownstreamStubServer(final int port) {
    downstreamStubServer =
        HttpServer.create()
            .port(port)
            .route(
                routes ->
                    routes.route(
                        request ->
                            request.uri().startsWith("/api/v1/books")
                                || request.uri().startsWith("/api/v1/auth")
                                || request.uri().startsWith("/api/v1/users")
                                || request.uri().startsWith("/api/v1/library")
                                || request.uri().startsWith("/api/v1/goals")
                                || request.uri().startsWith("/api/v1/reviews")
                                || request.uri().startsWith("/api/v1/notifications")
                                || request.uri().startsWith("/api/v1/admin"),
                        GatewayApplicationTests::handleDownstreamRequest))
            .bindNow();
    downstreamPort = downstreamStubServer.port();
    return downstreamStubServer;
  }

  private static void stopDownstreamStubServer() {
    if (downstreamStubServer != null && !downstreamStubServer.isDisposed()) {
      downstreamStubServer.disposeNow();
    }
  }

  private static reactor.core.publisher.Mono<Void> handleDownstreamRequest(
      final reactor.netty.http.server.HttpServerRequest request,
      final reactor.netty.http.server.HttpServerResponse response) {
    response.header("X-Downstream-Path", request.uri());

    final String forwardedProto = request.requestHeaders().get("X-Forwarded-Proto");
    if (forwardedProto != null) {
      response.header("X-Downstream-Forwarded-Proto", forwardedProto);
    }

    final String forwardedHost = request.requestHeaders().get("X-Forwarded-Host");
    if (forwardedHost != null) {
      response.header("X-Downstream-Forwarded-Host", forwardedHost);
    }

    final String forwardedHeader = request.requestHeaders().get("Forwarded");
    if (forwardedHeader != null) {
      response.header("X-Downstream-Forwarded", forwardedHeader);
    }

    final String traceparent = request.requestHeaders().get("traceparent");
    if (traceparent != null) {
      response.header("X-Downstream-Traceparent", traceparent);
    }

    final String uri = request.uri();
    if (uri.contains("probe=unauthorized")) {
      response.status(401).send();
      return response.then();
    }

    if (uri.contains("probe=forbidden")) {
      response.status(403).send();
      return response.then();
    }

    if (uri.contains("probe=upstream-500")) {
      response.status(500).send();
      return response.then();
    }

    if (uri.contains("probe=unreachable")) {
      response.status(503).send();
      return response.then();
    }

    if (uri.contains("cors-probe")) {
      response
          .status(200)
          .header(ACCESS_CONTROL_ALLOW_ORIGIN, "https://downstream.local")
          .header(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
          .sendString(Mono.just("cors-probe"));
      return response.then();
    }

    response.status(200).sendString(Mono.just("ok"));
    return response.then();
  }
}
