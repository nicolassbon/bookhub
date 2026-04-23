package com.bookhub.identity.web.auth;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.infrastructure.persistence.RefreshTokenJpaRepository;
import com.bookhub.identity.infrastructure.persistence.UserJpaRepository;
import com.bookhub.identity.support.PostgreSqlIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class LoginIntegrationTest extends PostgreSqlIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserJpaRepository userJpaRepository;

  @Autowired private RefreshTokenJpaRepository refreshTokenJpaRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    refreshTokenJpaRepository.deleteAll();
    userJpaRepository.deleteAll();
  }

  @Test
  @DisplayName("Should authenticate existing user and return signed access token")
  void shouldAuthenticateExistingUserAndReturnSignedAccessToken() throws Exception {
    final var existingUser =
        AuthIntegrationFixture.user(
            "nico",
            "nico@example.com",
            passwordEncoder.encode("StrongPassword123!"),
            "Nicolas Bon");
    final var savedUser = userJpaRepository.save(existingUser);

    final String requestBody =
        """
                {
                  "email": "NICO@Example.com",
                  "password": "StrongPassword123!"
                }
                """;

    mockMvc
        .perform(
            post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isOk())
        .andExpect(header().string("Set-Cookie", containsString("refresh_token=")))
        .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
        .andExpect(header().string("Set-Cookie", containsString("SameSite=Strict")))
        .andExpect(
            jsonPath(
                "$.accessToken",
                matchesPattern("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$")))
        .andExpect(jsonPath("$.expiresIn").value(3600))
        .andExpect(jsonPath("$.refreshToken").doesNotExist())
        .andExpect(jsonPath("$.user.userId").value(savedUser.getId().toString()))
        .andExpect(jsonPath("$.user.username").value("nico"))
        .andExpect(jsonPath("$.user.displayName").value("Nicolas Bon"))
        .andExpect(jsonPath("$.user.role").value("USER"));
  }

  @Test
  @DisplayName("Should return 401 with structured error when password is invalid")
  void shouldReturn401WithStructuredErrorWhenPasswordIsInvalid() throws Exception {
    final var existingUser =
        AuthIntegrationFixture.user(
            "nico",
            "nico@example.com",
            passwordEncoder.encode("StrongPassword123!"),
            "Nicolas Bon");
    userJpaRepository.save(existingUser);

    final String requestBody =
        """
                {
                  "email": "nico@example.com",
                  "password": "WrongPassword123!"
                }
                """;

    mockMvc
        .perform(
            post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.error").value("Unauthorized"))
        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
        .andExpect(jsonPath("$.message").value("Invalid email or password"))
        .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
  }
}
