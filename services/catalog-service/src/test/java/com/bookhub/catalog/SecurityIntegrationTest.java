package com.bookhub.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.catalog.support.TestRsaKeys;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

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
    registry.add("jwt.rsa.public-key", () -> TestRsaKeys.PUBLIC_2048);
  }

  @Autowired private MockMvc mockMvc;

  @Test
  void publicGetAccessToBooksReturnsOkWithoutToken() throws Exception {
    mockMvc
        .perform(get("/api/v1/books").queryParam("q", "hobbit"))
        .andExpect(
            result -> {
              final int responseStatus = result.getResponse().getStatus();
              assertThat(responseStatus)
                  .as("Public GET /api/v1/books must not return 401")
                  .isIn(200, 503);
            });
    mockMvc
        .perform(get("/api/v1/books/123e4567-e89b-12d3-a456-426614174000"))
        .andExpect(
            result -> {
              int status = result.getResponse().getStatus();
              assertThat(status)
                  .as("Public GET /api/v1/books/{id} must not return 401")
                  .isIn(200, 404, 400, 503);
            });
  }

  @Test
  void accessToInternalEndpointsReturnsUnauthorizedWithoutToken() throws Exception {
    mockMvc
        .perform(get("/api/v1/internal/books/123e4567-e89b-12d3-a456-426614174000"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void internalEndpointReturnsUnauthorizedWithoutToken() throws Exception {
    mockMvc
        .perform(get("/api/v1/internal/books/00000000-0000-0000-0000-000000000000"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(authorities = {"ROLE_USER"})
  void internalEndpointReturnsForbiddenWithUserRole() throws Exception {
    mockMvc
        .perform(get("/api/v1/internal/books/123e4567-e89b-12d3-a456-426614174000"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = {"ROLE_SERVICE"})
  void internalEndpointReturnsOkOrNotFoundWithServiceRole() throws Exception {
    mockMvc
        .perform(get("/api/v1/internal/books/123e4567-e89b-12d3-a456-426614174000"))
        .andExpect(
            result -> {
              int status = result.getResponse().getStatus();
              assertThat(status)
                  .as("Internal endpoint with ROLE_SERVICE must not return 401 or 403")
                  .isIn(200, 404);
            });
  }

  @Test
  void internalEndpointAcceptsRealServiceJwt() throws Exception {
    final String serviceJwt = signServiceJwt("library-service", "SERVICE");

    mockMvc
        .perform(
            get("/api/v1/internal/books/123e4567-e89b-12d3-a456-426614174000")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceJwt))
        .andExpect(
            result -> {
              int status = result.getResponse().getStatus();
              assertThat(status)
                  .as("Internal endpoint with real service JWT (role=SERVICE) must not return 401 or 403")
                  .isIn(200, 404);
            });
  }

  @Test
  void internalEndpointRejectsRealUserJwt() throws Exception {
    final String userJwt = signServiceJwt("user-123", "USER");

    mockMvc
        .perform(
            get("/api/v1/internal/books/123e4567-e89b-12d3-a456-426614174000")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userJwt))
        .andExpect(status().isForbidden());
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

  private static String signServiceJwt(final String subject, final String role) throws Exception {
    final KeyFactory keyFactory = KeyFactory.getInstance("RSA");

    final String privateKeyPem = TestRsaKeys.PRIVATE_2048;
    final String privateKeyBase64 =
        privateKeyPem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
    final RSAPrivateKey privateKey =
        (RSAPrivateKey)
            keyFactory.generatePrivate(
                new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64)));

    final String publicKeyPem = TestRsaKeys.PUBLIC_2048;
    final String publicKeyBase64 =
        publicKeyPem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");
    final RSAPublicKey publicKey =
        (RSAPublicKey)
            keyFactory.generatePublic(
                new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64)));

    final RSAKey rsaKey =
        new RSAKey.Builder(publicKey).privateKey(privateKey).keyID("test-key").build();
    final NimbusJwtEncoder encoder =
        new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));

    final JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .subject(subject)
            .issuer("http://test-issuer")
            .audience(List.of("bookhub-client"))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .claim("role", role)
            .build();

    final JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
    return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }
}
