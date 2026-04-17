package com.bookhub.identity.web.auth;

import static org.assertj.core.api.Assertions.assertThat;
import com.bookhub.identity.application.auth.PasswordResetTokenHasher;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.domain.auth.MailSenderPort;
import com.bookhub.identity.domain.auth.PasswordResetToken;
import com.bookhub.identity.infrastructure.persistence.RefreshTokenJpaRepository;
import com.bookhub.identity.infrastructure.persistence.PasswordResetTokenJpaRepository;
import com.bookhub.identity.infrastructure.persistence.UserJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordRecoveryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PasswordResetTokenJpaRepository passwordResetTokenJpaRepository;

    @Autowired
    private RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenHasher passwordResetTokenHasher;

    @MockitoBean
    private MailSenderPort mailSenderPort;

    @BeforeEach
    void setUp() {
        refreshTokenJpaRepository.deleteAll();
        passwordResetTokenJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Should clear refresh tokens before deleting users in test setup")
    void shouldClearRefreshTokensBeforeDeletingUsersInTestSetup() {
        final var user = userJpaRepository.save(AuthIntegrationFixture.user("nico",
                "nico@example.com", passwordEncoder.encode("StrongPassword123!"), "Nico"));

        refreshTokenJpaRepository.save(AuthIntegrationFixture.refreshToken(
                UUID.fromString("2d99ac5e-722f-451f-abd4-ac035f4f1420"), user,
                Instant.now().plusSeconds(3600)));

        setUp();

        assertThat(refreshTokenJpaRepository.count()).isZero();
        assertThat(userJpaRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should complete password reset flow and reject token reuse")
    void shouldCompletePasswordResetFlowAndRejectTokenReuse() throws Exception {
        final var user = userJpaRepository.save(AuthIntegrationFixture.user("nico",
                "nico@example.com", passwordEncoder.encode("StrongPassword123!"), "Nico"));

        final String forgotPasswordBody = """
                {
                  "email": "nico@example.com"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                .content(forgotPasswordBody)).andExpect(status().isOk());

        final var createdTokens = passwordResetTokenJpaRepository.findAllByUserId(user.getId());
        assertThat(createdTokens).hasSize(1);
        final String tokenHash = createdTokens.getFirst().getTokenHash();
        assertThat(tokenHash)
                .isNotBlank()
                .doesNotContain("-");

        final ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailSenderPort).sendPasswordResetEmail(org.mockito.ArgumentMatchers.eq("nico@example.com"), tokenCaptor.capture());
        final String token = tokenCaptor.getValue();

        final String resetBody = """
                {
                  "token": "%s",
                  "newPassword": "NewStrongPassword123!"
                }
                """.formatted(token);

        mockMvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(resetBody)).andExpect(status().isOk());

        final var updatedUser = userJpaRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("NewStrongPassword123!", updatedUser.getPasswordHash()))
                .isTrue();
        assertThat(passwordResetTokenJpaRepository.findByTokenHash(tokenHash)).isEmpty();

        mockMvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(resetBody)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PASSWORD_RESET_TOKEN"));
    }

    @Test
    @DisplayName("Should reject expired reset token")
    void shouldRejectExpiredResetToken() throws Exception {
        final var user = userJpaRepository.save(AuthIntegrationFixture.user("nico",
                "nico@example.com", passwordEncoder.encode("StrongPassword123!"), "Nico"));

        passwordResetTokenJpaRepository.save(PasswordResetToken.rehydrate(
                UUID.fromString("63553f40-4298-4768-b6b2-71385163467b"),
                passwordResetTokenHasher.hash("expired-token"),
                user.getId(),
                Instant.now().minusSeconds(60)));

        final String resetBody = """
                {
                  "token": "expired-token",
                  "newPassword": "NewStrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(resetBody)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PASSWORD_RESET_TOKEN"));
    }
}
