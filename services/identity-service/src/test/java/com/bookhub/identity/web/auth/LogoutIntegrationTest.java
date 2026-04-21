package com.bookhub.identity.web.auth;

import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.application.auth.RefreshTokenHasher;
import com.bookhub.identity.infrastructure.persistence.RefreshTokenJpaRepository;
import com.bookhub.identity.infrastructure.persistence.UserJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LogoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenHasher refreshTokenHasher;

    @BeforeEach
    void setUp() {
        refreshTokenJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Should revoke refresh token and clear cookie on logout")
    void shouldRevokeRefreshTokenAndClearCookieOnLogout() throws Exception {
        final var user = userJpaRepository.save(AuthIntegrationFixture.user(
                "nico",
                "nico@example.com",
                passwordEncoder.encode("StrongPassword123!"),
                "Nicolas Bon"));

        final UUID tokenValue = UUID.fromString("e2b2f5d2-a101-4a3e-b1f2-250d58df1309");
        refreshTokenJpaRepository.save(AuthIntegrationFixture.refreshToken(tokenValue, user, Instant.now().plusSeconds(3600)));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", tokenValue.toString())))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));

        assertThat(refreshTokenJpaRepository.findById(refreshTokenHasher.hash(tokenValue.toString())))
                .isPresent()
                .get()
                .matches(refreshToken -> refreshToken.isRevoked(), "logout should revoke persisted token row");
    }
}
