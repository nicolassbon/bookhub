package com.bookhub.identity.web.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.application.auth.ServiceTokenIssuer;
import com.bookhub.identity.application.auth.TokenIssuer.IssuedTokenPair;
import com.bookhub.identity.application.auth.ratelimit.AuthRateLimitStore;
import com.bookhub.identity.application.auth.ratelimit.RateLimitDecision;
import com.bookhub.identity.config.JwtKeyConfig;
import com.bookhub.identity.config.RefreshTokenProperties;
import com.bookhub.identity.config.SecurityConfig;
import com.bookhub.identity.domain.auth.ServicePrincipal;
import com.bookhub.identity.web.error.GlobalExceptionHandler;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ServiceAuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtKeyConfig.class})
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "service.client-id=test-client",
      "service.client-secret=test-secret",
      "auth.rate-limit.service-token.max-attempts=1",
      "auth.rate-limit.service-token.window-seconds=60"
    })
class ServiceAuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ServiceTokenIssuer serviceTokenIssuer;

  @MockitoBean private Clock clock;

  @MockitoBean private AuthRateLimitStore authRateLimitStore;

  @MockitoBean private RefreshTokenProperties refreshTokenProperties;

  @BeforeEach
  void setUp() {
    when(clock.instant()).thenReturn(Instant.parse("2026-05-06T00:00:00Z"));
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    when(refreshTokenProperties.cookieName()).thenReturn("refresh_token");
    when(refreshTokenProperties.cookiePath()).thenReturn("/api/v1/auth");
    when(refreshTokenProperties.cookieSameSite()).thenReturn("Strict");
    when(refreshTokenProperties.cookieSecure()).thenReturn(false);
    when(authRateLimitStore.consume(any(), anyInt(), any(Duration.class)))
        .thenReturn(RateLimitDecision.allowed(99, Duration.ofSeconds(60)));
  }

  @Test
  @DisplayName("Should return 200 with JWT when valid Basic Auth credentials are provided")
  void shouldReturn200WithJwtWhenValidBasicAuthProvided() throws Exception {
    final IssuedTokenPair tokenPair =
        IssuedTokenPair.builder().accessToken("svc-jwt-token").expiresIn(3600).build();

    when(serviceTokenIssuer.issueFor(any(ServicePrincipal.class))).thenReturn(tokenPair);

    final String authHeader = basicAuthHeader("test-client:test-secret");

    mockMvc
        .perform(
            post("/api/v1/auth/service-token")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("svc-jwt-token"))
        .andExpect(jsonPath("$.expiresIn").value(3600));

    verify(serviceTokenIssuer).issueFor(new ServicePrincipal("test-client"));
  }

  @Test
  void shouldReturn429WhenServiceTokenRateLimitIsExceeded() throws Exception {
    final IssuedTokenPair tokenPair =
        IssuedTokenPair.builder().accessToken("svc-jwt-token").expiresIn(3600).build();
    final String authHeader = basicAuthHeader("test-client:test-secret");

    when(serviceTokenIssuer.issueFor(any(ServicePrincipal.class))).thenReturn(tokenPair);
    when(authRateLimitStore.consume(any(), anyInt(), any(Duration.class)))
        .thenReturn(
            RateLimitDecision.allowed(0, Duration.ofSeconds(60)),
            RateLimitDecision.blocked(Duration.ofSeconds(60)));

    mockMvc
        .perform(
            post("/api/v1/auth/service-token")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/v1/auth/service-token")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.status").value(429))
        .andExpect(jsonPath("$.code").value("RATE_LIMIT_EXCEEDED"))
        .andExpect(jsonPath("$.path").value("/api/v1/auth/service-token"));
  }

  @Test
  @DisplayName("Should return 401 when no Authorization header is present")
  void shouldReturn401WhenNoAuthorizationHeader() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/service-token").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.path").value("/api/v1/auth/service-token"));

    verify(serviceTokenIssuer, never()).issueFor(any());
  }

  @Test
  @DisplayName("Should return 401 when Basic Auth credentials are invalid")
  void shouldReturn401WhenInvalidCredentials() throws Exception {
    final String authHeader = basicAuthHeader("test-client:wrong-secret");

    mockMvc
        .perform(
            post("/api/v1/auth/service-token")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.path").value("/api/v1/auth/service-token"));

    verify(serviceTokenIssuer, never()).issueFor(any());
  }

  @Test
  @DisplayName("Should return 401 when Authorization header is not Basic scheme")
  void shouldReturn401WhenNotBasicScheme() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/service-token")
                .header("Authorization", "Bearer some-token")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    verify(serviceTokenIssuer, never()).issueFor(any());
  }

  @Test
  @DisplayName("Should accept Basic auth scheme case-insensitively (lowercase)")
  void shouldAcceptLowercaseBasicScheme() throws Exception {
    final IssuedTokenPair tokenPair =
        IssuedTokenPair.builder().accessToken("svc-jwt-token").expiresIn(3600).build();

    when(serviceTokenIssuer.issueFor(any(ServicePrincipal.class))).thenReturn(tokenPair);

    final String authHeader =
        "basic " + Base64.getEncoder().encodeToString("test-client:test-secret".getBytes());

    mockMvc
        .perform(
            post("/api/v1/auth/service-token")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("svc-jwt-token"));
  }

  @Test
  @DisplayName("Should accept Basic auth scheme case-insensitively (uppercase)")
  void shouldAcceptUppercaseBasicScheme() throws Exception {
    final IssuedTokenPair tokenPair =
        IssuedTokenPair.builder().accessToken("svc-jwt-token").expiresIn(3600).build();

    when(serviceTokenIssuer.issueFor(any(ServicePrincipal.class))).thenReturn(tokenPair);

    final String authHeader =
        "BASIC " + Base64.getEncoder().encodeToString("test-client:test-secret".getBytes());

    mockMvc
        .perform(
            post("/api/v1/auth/service-token")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("svc-jwt-token"));
  }

  @Test
  @DisplayName("Should return 401 when Basic header has no credentials after scheme")
  void shouldReturn401WhenBasicHeaderHasNoCredentials() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/service-token")
                .header("Authorization", "Basic ")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

    verify(serviceTokenIssuer, never()).issueFor(any());
  }

  @Test
  @DisplayName("Should return 401 when Basic header has malformed Base64")
  void shouldReturn401WhenBasicHeaderHasMalformedBase64() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/service-token")
                .header("Authorization", "Basic !!!!")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

    verify(serviceTokenIssuer, never()).issueFor(any());
  }

  @Test
  @DisplayName("Should return 401 when Basic header has no colon separator")
  void shouldReturn401WhenBasicHeaderHasNoColon() throws Exception {
    final String authHeader =
        "Basic " + Base64.getEncoder().encodeToString("clientWithoutColon".getBytes());

    mockMvc
        .perform(
            post("/api/v1/auth/service-token")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

    verify(serviceTokenIssuer, never()).issueFor(any());
  }

  private String basicAuthHeader(final String credentials) {
    return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
  }
}
