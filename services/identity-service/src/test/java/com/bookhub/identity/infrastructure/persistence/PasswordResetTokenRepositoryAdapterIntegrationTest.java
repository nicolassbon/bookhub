package com.bookhub.identity.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.bookhub.identity.domain.auth.PasswordResetToken;
import com.bookhub.identity.domain.auth.PasswordResetTokenRepository;
import com.bookhub.identity.support.PostgreSqlIntegrationTest;
import com.bookhub.identity.web.auth.AuthIntegrationFixture;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordResetTokenRepositoryAdapterIntegrationTest extends PostgreSqlIntegrationTest {

  @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;

  @Autowired private PasswordResetTokenJpaRepository passwordResetTokenJpaRepository;

  @Autowired private UserJpaRepository userJpaRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("Should save, find and delete password reset token")
  void shouldSaveFindAndDeletePasswordResetToken() {
    final var user =
        userJpaRepository.save(
            AuthIntegrationFixture.user(
                "nico", "nico@example.com", passwordEncoder.encode("StrongPassword123!"), "Nico"));

    final PasswordResetToken token =
        PasswordResetToken.rehydrate(
            UUID.fromString("3a5f37c4-f975-4f5f-8190-24338f4dd58e"),
            "ab6bd217f65846869dacbc93d83512fdhash",
            user.getId(),
            Instant.parse("2026-04-12T21:00:00Z"));

    passwordResetTokenRepository.save(token);

    final var persistedToken =
        passwordResetTokenRepository.findByTokenHash("ab6bd217f65846869dacbc93d83512fdhash");
    assertThat(persistedToken).isPresent();
    assertThat(persistedToken.get().getUserId()).isEqualTo(user.getId());

    passwordResetTokenRepository.delete(persistedToken.get());

    assertThat(passwordResetTokenRepository.findByTokenHash("ab6bd217f65846869dacbc93d83512fdhash"))
        .isEmpty();
  }

  @Test
  @DisplayName("Should replace token atomically and keep only one token per user")
  void shouldReplaceTokenAtomicallyAndKeepOneTokenPerUser() {
    final var user =
        userJpaRepository.save(
            AuthIntegrationFixture.user(
                "nico", "nico@example.com", passwordEncoder.encode("StrongPassword123!"), "Nico"));

    passwordResetTokenRepository.replaceForUser(
        user.getId(), "token-one-hash", Instant.parse("2026-04-12T21:00:00Z"));
    passwordResetTokenRepository.replaceForUser(
        user.getId(), "token-two-hash", Instant.parse("2026-04-12T21:05:00Z"));

    assertThat(passwordResetTokenJpaRepository.findByTokenHash("token-one-hash")).isEmpty();
    assertThat(passwordResetTokenJpaRepository.findByTokenHash("token-two-hash")).isPresent();
  }

  @Test
  @DisplayName("Should consume token hash atomically")
  void shouldConsumeTokenHashAtomically() {
    final var user =
        userJpaRepository.save(
            AuthIntegrationFixture.user(
                "nico", "nico@example.com", passwordEncoder.encode("StrongPassword123!"), "Nico"));

    passwordResetTokenRepository.replaceForUser(
        user.getId(), "token-consume-hash", Instant.parse("2026-04-12T21:00:00Z"));

    final var consumedUserId =
        passwordResetTokenRepository.consumeUserIdByTokenHash(
            "token-consume-hash", Instant.parse("2026-04-12T20:00:00Z"));
    final var consumedAgain =
        passwordResetTokenRepository.consumeUserIdByTokenHash(
            "token-consume-hash", Instant.parse("2026-04-12T20:00:01Z"));

    assertThat(consumedUserId).contains(user.getId());
    assertThat(consumedAgain).isEmpty();
  }
}
