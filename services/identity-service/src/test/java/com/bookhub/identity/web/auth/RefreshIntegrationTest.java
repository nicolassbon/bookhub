package com.bookhub.identity.web.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.domain.auth.RefreshToken;
import com.bookhub.identity.infrastructure.persistence.RefreshTokenJpaRepository;
import com.bookhub.identity.infrastructure.persistence.UserJpaRepository;
import com.bookhub.identity.support.PostgreSqlIntegrationTest;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class RefreshIntegrationTest extends PostgreSqlIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserJpaRepository userJpaRepository;

  @Autowired private RefreshTokenJpaRepository refreshTokenJpaRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("Should rotate refresh token and return new access token")
  void shouldRotateRefreshTokenAndReturnNewAccessToken() throws Exception {
    final var user =
        userJpaRepository.save(
            AuthIntegrationFixture.user(
                "nico",
                "nico@example.com",
                passwordEncoder.encode("StrongPassword123!"),
                "Nicolas Bon"));

    final UUID oldToken = UUID.fromString("d7cc2a0f-ea1a-4f74-8e64-3f3f4a5ba723");
    refreshTokenJpaRepository.save(
        AuthIntegrationFixture.refreshToken(oldToken, user, Instant.now().plusSeconds(3600)));

    final var response =
        mockMvc
            .perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .cookie(new jakarta.servlet.http.Cookie("refresh_token", oldToken.toString())))
            .andExpect(status().isOk())
            .andExpect(header().string("Set-Cookie", containsString("refresh_token=")))
            .andExpect(
                jsonPath(
                    "$.accessToken",
                    matchesPattern("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$")))
            .andExpect(jsonPath("$.refreshToken").doesNotExist())
            .andReturn();

    final String setCookie = response.getResponse().getHeader("Set-Cookie");
    final UUID rotatedToken = extractRefreshToken(setCookie);

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new jakarta.servlet.http.Cookie("refresh_token", oldToken.toString())))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"))
        .andExpect(jsonPath("$.path").value("/api/v1/auth/refresh"));

    assertThat(
            refreshTokenJpaRepository.findById(AuthIntegrationFixture.refreshTokenHash(oldToken)))
        .isPresent()
        .get()
        .matches(refreshToken -> refreshToken.isRevoked(), "old token should be revoked");
    assertThat(
            refreshTokenJpaRepository.findById(
                AuthIntegrationFixture.refreshTokenHash(rotatedToken)))
        .isPresent()
        .get()
        .matches(refreshToken -> !refreshToken.isRevoked(), "new token should be active");
    assertThat(rotatedToken).isNotEqualTo(oldToken);
  }

  @Test
  @DisplayName("Should return 401 when refresh token is invalid")
  void shouldReturn401WhenRefreshTokenIsInvalid() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("refresh_token", "not-a-valid-token")))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
  }

  @Test
  @DisplayName("Should return 401 when refresh token is expired")
  void shouldReturn401WhenRefreshTokenIsExpired() throws Exception {
    final var user =
        userJpaRepository.save(
            AuthIntegrationFixture.user(
                "nico",
                "nico@example.com",
                passwordEncoder.encode("StrongPassword123!"),
                "Nicolas Bon"));

    final UUID expiredToken = UUID.fromString("34d60b38-596f-4810-9534-bc2c9f5fed04");
    refreshTokenJpaRepository.save(
        AuthIntegrationFixture.refreshToken(expiredToken, user, Instant.now().minusSeconds(120)));

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("refresh_token", expiredToken.toString())))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
  }

  @Test
  @DisplayName("Should return 401 when refresh token is revoked")
  void shouldReturn401WhenRefreshTokenIsRevoked() throws Exception {
    final var user =
        userJpaRepository.save(
            AuthIntegrationFixture.user(
                "nico",
                "nico@example.com",
                passwordEncoder.encode("StrongPassword123!"),
                "Nicolas Bon"));

    final UUID revokedToken = UUID.fromString("cb512f1d-6285-4c01-abee-a1553cc4065c");
    refreshTokenJpaRepository.save(
        RefreshToken.rehydrate(
            AuthIntegrationFixture.refreshTokenHash(revokedToken),
            user,
            Instant.now().plusSeconds(3600),
            true,
            null));

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("refresh_token", revokedToken.toString())))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
  }

  private UUID extractRefreshToken(final String setCookieHeader) {
    final String[] parts = setCookieHeader.split(";");
    final String cookiePart = parts[0];
    final String[] keyValue = cookiePart.split("=", 2);
    assertThat(keyValue[0].toLowerCase(Locale.ROOT)).isEqualTo("refresh_token");
    return UUID.fromString(keyValue[1]);
  }
}
