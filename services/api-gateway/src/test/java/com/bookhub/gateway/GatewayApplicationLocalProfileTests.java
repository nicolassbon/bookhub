package com.bookhub.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class GatewayApplicationLocalProfileTests {

  @Autowired private RouteDefinitionLocator routeDefinitionLocator;

  @Test
  void contextLoads() {}

  @Test
  void shouldConfigureRoutesForAllServicesInLocalProfile() {
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
            "library-book-reviews-route",
            "library-notifications-route",
            "library-admin-route");

    assertThat(routeDefinitions)
        .anyMatch(
            routeDefinition ->
                routeDefinition.getId().equals("identity-auth-route")
                    && routeDefinition.getUri().toString().equals("http://localhost:8081"));

    assertThat(routeDefinitions)
        .anyMatch(
            routeDefinition ->
                routeDefinition.getId().equals("catalog-books-route")
                    && routeDefinition.getUri().toString().equals("http://localhost:8082"));

    assertThat(routeDefinitions)
        .anyMatch(
            routeDefinition ->
                routeDefinition.getId().equals("library-route")
                    && routeDefinition.getUri().toString().equals("http://localhost:8083"));

    assertThat(routeDefinitions)
        .anyMatch(
            routeDefinition ->
                routeDefinition.getId().equals("library-book-reviews-route")
                    && routeDefinition.getUri().toString().equals("http://localhost:8083")
                    && routeDefinition.getPredicates().stream()
                        .anyMatch(
                            predicate ->
                                predicate.getArgs().values().stream()
                                    .anyMatch(
                                        value ->
                                            value
                                                .toString()
                                                .contains("/api/v1/books/{bookId}/reviews"))));

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
        .as("library-book-reviews-route must precede catalog-books-route so reviews reach library")
        .isLessThan(catalogBooksRouteIndex);
  }
}
