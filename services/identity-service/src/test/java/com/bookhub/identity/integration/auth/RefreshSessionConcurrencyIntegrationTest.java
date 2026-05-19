package com.bookhub.identity.integration.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.bookhub.identity.application.auth.InvalidRefreshTokenException;
import com.bookhub.identity.application.auth.RefreshSessionResult;
import com.bookhub.identity.application.auth.RefreshSessionService;
import com.bookhub.identity.domain.auth.RefreshToken;
import com.bookhub.identity.application.auth.RefreshTokenHasher;
import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRole;
import com.bookhub.identity.infrastructure.persistence.RefreshTokenJpaRepository;
import com.bookhub.identity.infrastructure.persistence.UserJpaRepository;
import com.bookhub.identity.support.PostgreSqlIntegrationTest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RefreshSessionConcurrencyIntegrationTest extends PostgreSqlIntegrationTest {

  private static final int CONCURRENT_THREADS = 12;

  @Autowired private RefreshSessionService refreshSessionService;
  @Autowired private UserJpaRepository userJpaRepository;
  @Autowired private RefreshTokenJpaRepository refreshTokenJpaRepository;
  @Autowired private RefreshTokenHasher refreshTokenHasher;

  private String validRefreshTokenValue;

  @BeforeEach
  void setUp() {
    refreshTokenJpaRepository.deleteAll();
    userJpaRepository.deleteAll();

    final User user =
        userJpaRepository.save(
            User.create(
                "concurrency-test-user",
                "concurrency@example.test",
                "ignored-hash",
                "Concurrency User",
                UserRole.USER));

    validRefreshTokenValue = UUID.randomUUID().toString();
    final String tokenHash = refreshTokenHasher.hash(validRefreshTokenValue);

    refreshTokenJpaRepository.save(
        RefreshToken.issue(tokenHash, user, Instant.now().plus(1, ChronoUnit.HOURS)));
  }

  @Test
  @DisplayName(
      "Only one concurrent refresh attempt should succeed; all others must be rejected as invalid")
  void onlyOneRefreshAttemptShouldSucceedUnderConcurrentLoad() throws InterruptedException {
    final CountDownLatch startGate = new CountDownLatch(1);
    final AtomicInteger successCount = new AtomicInteger(0);
    final AtomicInteger rejectedCount = new AtomicInteger(0);
    final AtomicInteger unexpectedErrorCount = new AtomicInteger(0);

    final ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
    final List<Future<?>> futures = new ArrayList<>(CONCURRENT_THREADS);

    for (int i = 0; i < CONCURRENT_THREADS; i++) {
      futures.add(
          executor.submit(
              () -> {
                try {
                  startGate.await();
                  final RefreshSessionResult result =
                      refreshSessionService.refresh(validRefreshTokenValue);
                  if (result != null) {
                    successCount.incrementAndGet();
                  }
                } catch (InvalidRefreshTokenException e) {
                  rejectedCount.incrementAndGet();
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                } catch (Exception e) {
                  unexpectedErrorCount.incrementAndGet();
                }
              }));
    }

    startGate.countDown();
    executor.shutdown();
    executor.awaitTermination(30, TimeUnit.SECONDS);

    assertThat(successCount.get())
        .as("Exactly one refresh attempt should succeed")
        .isEqualTo(1);

    assertThat(rejectedCount.get())
        .as("All other concurrent attempts should be rejected as invalid")
        .isEqualTo(CONCURRENT_THREADS - 1);

    assertThat(unexpectedErrorCount.get())
        .as("No unexpected errors should occur during concurrent refresh")
        .isEqualTo(0);
  }
}
