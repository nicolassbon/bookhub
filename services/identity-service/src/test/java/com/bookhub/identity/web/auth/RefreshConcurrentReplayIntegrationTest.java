package com.bookhub.identity.web.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.bookhub.identity.infrastructure.persistence.RefreshTokenJpaRepository;
import com.bookhub.identity.infrastructure.persistence.UserJpaRepository;
import com.bookhub.identity.support.PostgreSqlIntegrationTest;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc
class RefreshConcurrentReplayIntegrationTest extends PostgreSqlIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserJpaRepository userJpaRepository;

  @Autowired private RefreshTokenJpaRepository refreshTokenJpaRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("Concurrent refresh with same token should yield exactly one success and one failure")
  void concurrentRefreshWithSameTokenShouldYieldExactlyOneSuccessAndOneFailure() throws Exception {
    final var user =
        userJpaRepository.save(
            AuthIntegrationFixture.user(
                "concurrent-user",
                "concurrent@example.com",
                passwordEncoder.encode("StrongPassword123!"),
                "Concurrent User"));

    final UUID sharedToken = UUID.randomUUID();
    refreshTokenJpaRepository.save(
        AuthIntegrationFixture.refreshToken(sharedToken, user, Instant.now().plusSeconds(3600)));

    final ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      final CompletableFuture<MvcResult> request1 =
          CompletableFuture.supplyAsync(() -> performRefresh(sharedToken), executor);
      final CompletableFuture<MvcResult> request2 =
          CompletableFuture.supplyAsync(() -> performRefresh(sharedToken), executor);

      final MvcResult result1 = request1.get();
      final MvcResult result2 = request2.get();

      final int status1 = result1.getResponse().getStatus();
      final int status2 = result2.getResponse().getStatus();

      final long successCount =
          java.util.stream.Stream.of(status1, status2).filter(s -> s == 200).count();
      final long failureCount =
          java.util.stream.Stream.of(status1, status2).filter(s -> s == 401).count();

      assertThat(successCount)
          .as(
              "Exactly one concurrent refresh should succeed (got status1=%d, status2=%d)",
              status1, status2)
          .isEqualTo(1);
      assertThat(failureCount)
          .as(
              "Exactly one concurrent refresh should fail with 401 (got status1=%d, status2=%d)",
              status1, status2)
          .isEqualTo(1);

      final String oldTokenHash = AuthIntegrationFixture.refreshTokenHash(sharedToken);
      assertThat(refreshTokenJpaRepository.findById(oldTokenHash))
          .isPresent()
          .get()
          .matches(
              refreshToken -> refreshToken.isRevoked(),
              "original token should be revoked after concurrent refresh");

      final long activeTokenCount =
          refreshTokenJpaRepository.findAll().stream()
              .filter(t -> !t.isRevoked() && t.getExpiresAt().isAfter(Instant.now()))
              .count();
      assertThat(activeTokenCount)
          .as("Only one new active refresh token should exist after concurrent replay")
          .isEqualTo(1);
    } finally {
      executor.shutdownNow();
    }
  }

  private MvcResult performRefresh(final UUID tokenValue) {
    try {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.post("/api/v1/auth/refresh")
                  .contentType(MediaType.APPLICATION_JSON)
                  .cookie(
                      new jakarta.servlet.http.Cookie("refresh_token", tokenValue.toString())))
          .andReturn();
    } catch (Exception exception) {
      throw new RuntimeException("Refresh request failed unexpectedly", exception);
    }
  }
}
