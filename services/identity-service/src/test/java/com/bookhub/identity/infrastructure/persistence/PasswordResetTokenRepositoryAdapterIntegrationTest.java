package com.bookhub.identity.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.bookhub.identity.domain.auth.PasswordResetToken;
import com.bookhub.identity.domain.auth.PasswordResetTokenRepository;
import com.bookhub.identity.web.auth.AuthIntegrationFixture;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PasswordResetTokenRepositoryAdapterIntegrationTest {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordResetTokenJpaRepository passwordResetTokenJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordResetTokenJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save, find and delete password reset token")
    void shouldSaveFindAndDeletePasswordResetToken() {
        final var user = userJpaRepository.save(AuthIntegrationFixture.user(
                "nico",
                "nico@example.com",
                passwordEncoder.encode("StrongPassword123!"),
                "Nico"));

        final PasswordResetToken token = PasswordResetToken.builder()
                .id(UUID.fromString("3a5f37c4-f975-4f5f-8190-24338f4dd58e"))
                .token("ab6bd217-f658-4686-9dac-bc93d83512fd")
                .userId(user.getId())
                .expiresAt(Instant.parse("2026-04-12T21:00:00Z"))
                .build();

        passwordResetTokenRepository.save(token);

        final var persistedToken = passwordResetTokenRepository.findByToken("ab6bd217-f658-4686-9dac-bc93d83512fd");
        assertThat(persistedToken).isPresent();
        assertThat(persistedToken.get().getUserId()).isEqualTo(user.getId());

        passwordResetTokenRepository.delete(persistedToken.get());

        assertThat(passwordResetTokenRepository.findByToken("ab6bd217-f658-4686-9dac-bc93d83512fd")).isEmpty();
    }

    @Test
    @DisplayName("Should delete all tokens by user id")
    void shouldDeleteAllTokensByUserId() {
        final var user = userJpaRepository.save(AuthIntegrationFixture.user(
                "nico",
                "nico@example.com",
                passwordEncoder.encode("StrongPassword123!"),
                "Nico"));

        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .id(UUID.fromString("771f238c-dd1d-4e5a-a634-c0dd84f5ec63"))
                .token("token-one")
                .userId(user.getId())
                .expiresAt(Instant.parse("2026-04-12T21:00:00Z"))
                .build());
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .id(UUID.fromString("8f5342b9-006c-4a77-bf3e-3d728f4f54ca"))
                .token("token-two")
                .userId(user.getId())
                .expiresAt(Instant.parse("2026-04-12T21:05:00Z"))
                .build());

        passwordResetTokenRepository.deleteByUserId(user.getId());

        assertThat(passwordResetTokenJpaRepository.findByToken("token-one")).isEmpty();
        assertThat(passwordResetTokenJpaRepository.findByToken("token-two")).isEmpty();
    }
}
