package com.bookhub.identity.web.auth;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRole;
import com.bookhub.identity.infrastructure.persistence.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Should authenticate existing user and return signed access token")
    void shouldAuthenticateExistingUserAndReturnSignedAccessToken() throws Exception {
        final User existingUser = User.builder()
                .username("nico")
                .email("nico@example.com")
                .passwordHash(passwordEncoder.encode("StrongPassword123!"))
                .displayName("Nicolas Bon")
                .role(UserRole.USER)
                .build();
        final User savedUser = userJpaRepository.save(existingUser);

        final String requestBody = """
                {
                  "email": "NICO@Example.com",
                  "password": "StrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", matchesPattern("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$")))
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
        final User existingUser = User.builder()
                .username("nico")
                .email("nico@example.com")
                .passwordHash(passwordEncoder.encode("StrongPassword123!"))
                .displayName("Nicolas Bon")
                .role(UserRole.USER)
                .build();
        userJpaRepository.save(existingUser);

        final String requestBody = """
                {
                  "email": "nico@example.com",
                  "password": "WrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
    }
}
