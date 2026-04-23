package com.bookhub.identity.web.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRole;
import com.bookhub.identity.infrastructure.persistence.RefreshTokenJpaRepository;
import com.bookhub.identity.infrastructure.persistence.UserJpaRepository;
import com.bookhub.identity.support.PostgreSqlIntegrationTest;
import com.bookhub.identity.web.JwtTestTokenFactory;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class UserMeIntegrationTest extends PostgreSqlIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserJpaRepository userJpaRepository;

  @Autowired private RefreshTokenJpaRepository refreshTokenJpaRepository;

  @Autowired private JwtEncoder jwtEncoder;

  @BeforeEach
  void setUp() {
    refreshTokenJpaRepository.deleteAll();
    userJpaRepository.deleteAll();
  }

  @Test
  @DisplayName("Should return authenticated user profile when access token is valid")
  void shouldReturnAuthenticatedUserProfileWhenAccessTokenIsValid() throws Exception {
    final User savedUser =
        userJpaRepository.save(
            User.create("nico", "nico@example.com", "ignored", "Nicolas Bon", UserRole.USER));

    final String token =
        JwtTestTokenFactory.createAccessToken(
            jwtEncoder,
            savedUser.getId().toString(),
            savedUser.getUsername(),
            savedUser.getDisplayName(),
            savedUser.getRole().name(),
            savedUser.getEmail(),
            Instant.now(),
            Instant.now().plus(1, ChronoUnit.HOURS));

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(savedUser.getId().toString()))
        .andExpect(jsonPath("$.username").value("nico"))
        .andExpect(jsonPath("$.displayName").value("Nicolas Bon"))
        .andExpect(jsonPath("$.email").value("nico@example.com"))
        .andExpect(jsonPath("$.role").value("USER"));
  }

  @Test
  @DisplayName("Should return 401 when access token is missing")
  void shouldReturn401WhenAccessTokenIsMissing() throws Exception {
    mockMvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should return 401 when access token is invalid or expired")
  void shouldReturn401WhenAccessTokenIsInvalidOrExpired() throws Exception {
    final String expiredToken =
        JwtTestTokenFactory.createAccessToken(
            jwtEncoder,
            "6676f2d8-0f65-40ae-b102-66145e24f3fd",
            "nico",
            "Nicolas Bon",
            "USER",
            "nico@example.com",
            Instant.now().minus(2, ChronoUnit.HOURS),
            Instant.now().minus(1, ChronoUnit.HOURS));

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + expiredToken))
        .andExpect(status().isUnauthorized());
  }
}
