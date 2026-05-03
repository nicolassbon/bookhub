package com.bookhub.catalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SecurityIntegrationTest {

  static final PostgreSQLContainer POSTGRESQL_CONTAINER =
      new PostgreSQLContainer("postgres:16-alpine");

  static {
    POSTGRESQL_CONTAINER.start();
  }

  @DynamicPropertySource
  static void configureProperties(final DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    registry.add("spring.flyway.enabled", () -> true);
    registry.add("jwt.issuer", () -> "http://test-issuer");
    registry.add("jwt.audience", () -> "bookhub-client");
  }

  @Autowired private MockMvc mockMvc;

  @Test
  void publicGetAccessToBooksReturnsOkWithoutToken() throws Exception {
    mockMvc
        .perform(get("/api/v1/books").queryParam("q", "hobbit"))
        .andExpect(
            result -> {
              final int responseStatus = result.getResponse().getStatus();
              assert responseStatus == 200 || responseStatus == 503;
              assert responseStatus != 401;
            });
    // Use an existing book or a dummy UUID, if not found returns 404, which is expected for public
    // endpoint.
    // The requirement says "access", so 404 is also fine as long as it's not 401
    mockMvc
        .perform(get("/api/v1/books/123e4567-e89b-12d3-a456-426614174000"))
        .andExpect(
            result -> {
              int status = result.getResponse().getStatus();
              assert status == 200 || status == 404 || status == 400 || status == 503;
              assert status != 401;
            });
  }

  @Test
  void accessToInternalEndpointsReturnsOkOrNotFoundWithoutToken() throws Exception {
    mockMvc
        .perform(get("/api/v1/internal/books/123e4567-e89b-12d3-a456-426614174000"))
        .andExpect(
            result -> {
              int status = result.getResponse().getStatus();
              assert status == 200 || status == 404 || status == 400 || status == 503;
              assert status != 401;
            });
  }

  @Test
  void accessToUnmappedEndpointsReturnsUnauthorizedWithoutToken() throws Exception {
    mockMvc.perform(get("/api/v1/unprotected")).andExpect(status().isUnauthorized());
  }

  @Test
  void mutationAttemptsReturnUnauthorizedWithoutToken() throws Exception {
    mockMvc.perform(post("/api/v1/books")).andExpect(status().isUnauthorized());
    mockMvc.perform(put("/api/v1/books/123")).andExpect(status().isUnauthorized());
    mockMvc.perform(delete("/api/v1/books/123")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(authorities = {"ROLE_USER"})
  void mutationAttemptsReturnForbiddenWithValidTokenLackingAdminRole() throws Exception {
    mockMvc.perform(post("/api/v1/books")).andExpect(status().isForbidden());
    mockMvc.perform(put("/api/v1/books/123")).andExpect(status().isForbidden());
    mockMvc.perform(delete("/api/v1/books/123")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = {"ROLE_ADMIN"})
  void mutationAttemptsProceedWithValidTokenAndAdminRole() throws Exception {
    mockMvc
        .perform(post("/api/v1/books"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.status").value(405))
        .andExpect(jsonPath("$.error").value("Method Not Allowed"))
        .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
        .andExpect(jsonPath("$.path").value("/api/v1/books"));
    mockMvc
        .perform(put("/api/v1/books/123"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.status").value(405))
        .andExpect(jsonPath("$.error").value("Method Not Allowed"))
        .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
        .andExpect(jsonPath("$.path").value("/api/v1/books/123"));
    mockMvc
        .perform(delete("/api/v1/books/123"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.status").value(405))
        .andExpect(jsonPath("$.error").value("Method Not Allowed"))
        .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
        .andExpect(jsonPath("$.path").value("/api/v1/books/123"));
  }

  @Test
  void actuatorHealthAccessIsPublic() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
  }
}
