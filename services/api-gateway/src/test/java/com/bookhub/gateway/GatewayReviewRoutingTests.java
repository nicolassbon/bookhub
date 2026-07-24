package com.bookhub.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/**
 * Destination-sensitive proof that a review {@code GET} selects the library route before the broad
 * catalog books route. Unlike the shared-stub forwarding test, this suite points catalog and
 * library at two distinct downstream stubs so the selected forward destination is observable.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GatewayReviewRoutingTests {

  private static DisposableServer libraryDownstreamServer = startDownstreamStub("library", 0);
  private static DisposableServer catalogDownstreamServer = startDownstreamStub("catalog", 0);

  @DynamicPropertySource
  static void registerGatewayRoutes(final DynamicPropertyRegistry registry) {
    final String libraryBaseUrl = "http://localhost:" + libraryDownstreamServer.port();
    final String catalogBaseUrl = "http://localhost:" + catalogDownstreamServer.port();
    registry.add("IDENTITY_SERVICE_URL", () -> libraryBaseUrl);
    registry.add("CATALOG_SERVICE_URL", () -> catalogBaseUrl);
    registry.add("LIBRARY_SERVICE_URL", () -> libraryBaseUrl);
    registry.add("spring.cloud.gateway.server.webflux.trusted-proxies", () -> ".*");
  }

  @AfterAll
  static void stopStubServers() {
    libraryDownstreamServer.disposeNow();
    catalogDownstreamServer.disposeNow();
  }

  @Autowired private RouteDefinitionLocator routeDefinitionLocator;

  @Autowired private WebTestClient webTestClient;

  @LocalServerPort private int serverPort;

  @Test
  void shouldRouteBookReviewsToLibraryInsteadOfCatalog() {
    final String reviewPath =
        "/api/v1/books/00000000-0000-0000-0000-000000000002/reviews?probe=reviews";

    webTestClient
        .get()
        .uri("http://localhost:" + serverPort + reviewPath)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .valueEquals("X-Downstream-Service", "library")
        .expectHeader()
        .valueEquals("X-Downstream-Path", reviewPath);
  }

  @Test
  void shouldRoutePlainBookRequestToCatalogAndNotLibrary() {
    final String bookPath = "/api/v1/books/00000000-0000-0000-0000-000000000002?probe=books";

    webTestClient
        .get()
        .uri("http://localhost:" + serverPort + bookPath)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .valueEquals("X-Downstream-Service", "catalog")
        .expectHeader()
        .valueEquals("X-Downstream-Path", bookPath);
  }

  @Test
  void shouldRouteReviewRequestWithNumericBookIdToLibrary() {
    final String reviewPath = "/api/v1/books/42/reviews?probe=numeric";

    webTestClient
        .get()
        .uri("http://localhost:" + serverPort + reviewPath)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .valueEquals("X-Downstream-Service", "library")
        .expectHeader()
        .valueEquals("X-Downstream-Path", reviewPath);
  }

  @Test
  void shouldDefineLibraryReviewRouteBeforeCatalogBooksRoute() {
    final List<RouteDefinition> routeDefinitions =
        routeDefinitionLocator.getRouteDefinitions().collectList().block();

    assertThat(routeDefinitions).isNotNull();
    final int reviewRouteIndex =
        routeDefinitions.indexOf(
            routeDefinitions.stream()
                .filter(route -> route.getId().equals("library-book-reviews-route"))
                .findFirst()
                .orElseThrow());
    final int catalogBooksRouteIndex =
        routeDefinitions.indexOf(
            routeDefinitions.stream()
                .filter(route -> route.getId().equals("catalog-books-route"))
                .findFirst()
                .orElseThrow());

    assertThat(reviewRouteIndex)
        .as("library-book-reviews-route must precede catalog-books-route")
        .isLessThan(catalogBooksRouteIndex);
  }

  private static DisposableServer startDownstreamStub(final String serviceName, final int port) {
    return HttpServer.create()
        .port(port)
        .route(
            routes ->
                routes.route(
                    request -> true,
                    (request, response) ->
                        response
                            .header("X-Downstream-Service", serviceName)
                            .header("X-Downstream-Path", request.uri())
                            .status(200)
                            .sendString(Mono.just(serviceName))
                            .then()))
        .bindNow();
  }
}
