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

  private static final DisposableServer downstreamStubServer =
      HttpServer.create()
          .port(0)
          .route(
              routes ->
                  routes.route(
                      request ->
                          request.uri().startsWith("/api/v1/books")
                              || request.uri().startsWith("/api/v1/auth")
                              || request.uri().startsWith("/api/v1/users"),
                      (request, response) -> {
                        response.header("X-Downstream-Path", request.uri());

                        final String forwardedProto =
                            request.requestHeaders().get("X-Forwarded-Proto");
                        if (forwardedProto != null) {
                          response.header("X-Downstream-Forwarded-Proto", forwardedProto);
                        }

                        final String forwardedHost =
                            request.requestHeaders().get("X-Forwarded-Host");
                        if (forwardedHost != null) {
                          response.header("X-Downstream-Forwarded-Host", forwardedHost);
                        }

                        final String forwardedHeader = request.requestHeaders().get("Forwarded");
                        if (forwardedHeader != null) {
                          response.header("X-Downstream-Forwarded", forwardedHeader);
                        }

                        if (request.uri().contains("cors-probe")) {
                          return response
                              .status(200)
                              .header(ACCESS_CONTROL_ALLOW_ORIGIN, "https://downstream.local")
                              .header(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
                              .sendString(Mono.just("cors-probe"));
                        }

                        return response.status(200).sendString(Mono.just("ok"));
                      }))
          .bindNow();

  @DynamicPropertySource
  static void registerGatewayRoutes(final DynamicPropertyRegistry registry) {
    final String downstreamBaseUrl = "http://localhost:" + downstreamStubServer.port();
    registry.add("IDENTITY_SERVICE_URL", () -> downstreamBaseUrl);
    registry.add("CATALOG_SERVICE_URL", () -> downstreamBaseUrl);
    registry.add("spring.cloud.gateway.server.webflux.trusted-proxies", () -> ".*");
  }

  @AfterAll
  static void stopStubServer() {
    downstreamStubServer.disposeNow();
  }

  @Autowired private RouteDefinitionLocator routeDefinitionLocator;

  @Autowired private WebTestClient webTestClient;

  @LocalServerPort private int serverPort;

  @Test
  void contextLoads() {}

  @Test
  void shouldConfigureRoutesForIdentityAndCatalogServices() {
    final List<RouteDefinition> routeDefinitions =
        routeDefinitionLocator.getRouteDefinitions().collectList().block();

    assertThat(routeDefinitions).isNotNull();
    assertThat(routeDefinitions)
        .extracting(RouteDefinition::getId)
        .containsExactlyInAnyOrder(
            "identity-auth-route", "identity-users-route", "catalog-books-route");

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
}
