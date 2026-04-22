package com.bookhub.identity.web.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRole;
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
class RegisterIntegrationTest extends PostgreSqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        refreshTokenJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Should persist a new user with hashed password through register endpoint")
    void shouldPersistNewUserWithHashedPasswordThroughRegisterEndpoint() throws Exception {
        final String requestBody = """
                {
                  "username": "nico",
                  "email": "nico@example.com",
                  "password": "StrongPassword123!",
                  "displayName": "Nicolas Bon"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("nico"))
                .andExpect(jsonPath("$.email").value("nico@example.com"))
                .andExpect(jsonPath("$.displayName").value("Nicolas Bon"))
                .andExpect(jsonPath("$.role").value("USER"));

        final User persistedUser = userJpaRepository.findAll().getFirst();
        assertThat(persistedUser.getUsername()).isEqualTo("nico");
        assertThat(persistedUser.getEmail()).isEqualTo("nico@example.com");
        assertThat(persistedUser.getDisplayName()).isEqualTo("Nicolas Bon");
        assertThat(persistedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(persistedUser.getPasswordHash()).isNotEqualTo("StrongPassword123!");
        assertThat(passwordEncoder.matches("StrongPassword123!", persistedUser.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("Should reject duplicate email with conflict response")
    void shouldRejectDuplicateEmailWithConflictResponse() throws Exception {
        final User existingUser = User.create(
                "nico",
                "nico@example.com",
                passwordEncoder.encode("StrongPassword123!"),
                "Nicolas Bon",
                UserRole.USER);
        userJpaRepository.save(existingUser);

        final String duplicateRequestBody = """
                {
                  "username": "nico2",
                  "email": "nico@example.com",
                  "password": "StrongPassword123!",
                  "displayName": "Nicolas Bon 2"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateRequestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }
}
