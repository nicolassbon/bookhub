package com.bookhub.identity.integration.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class UserProfileFlowIntegrationTest extends PostgreSqlIntegrationTest {

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
  @DisplayName("Should use persisted values for me endpoint instead of stale token claims")
  void shouldUsePersistedValuesForMeEndpointInsteadOfStaleTokenClaims() throws Exception {
    final User savedUser =
        userJpaRepository.save(
            User.create("nico", "nico@example.com", "ignored", "Persisted Name", UserRole.USER));
    savedUser.updateProfile("Persisted Name", "Persisted Bio", "https://cdn.bookhub/persisted.png");
    userJpaRepository.save(savedUser);

    final String token =
        JwtTestTokenFactory.createAccessToken(
            jwtEncoder,
            savedUser.getId().toString(),
            "stale-username",
            "Stale Name",
            savedUser.getRole().name(),
            "stale@example.com",
            Instant.now(),
            Instant.now().plus(1, ChronoUnit.HOURS));

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("nico"))
        .andExpect(jsonPath("$.displayName").value("Persisted Name"))
        .andExpect(jsonPath("$.email").value("nico@example.com"))
        .andExpect(jsonPath("$.bio").value("Persisted Bio"))
        .andExpect(jsonPath("$.avatarUrl").value("https://cdn.bookhub/persisted.png"));
  }

  @Test
  @DisplayName("Should reflect profile patch immediately without token refresh")
  void shouldReflectProfilePatchImmediatelyWithoutTokenRefresh() throws Exception {
    final User savedUser =
        userJpaRepository.save(
            User.create("nico", "nico@example.com", "ignored", "Nico", UserRole.USER));

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
        .perform(
            patch("/api/v1/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      \"displayName\": \"Nicolas Bon\",
                      \"bio\": \"Updated bio\",
                      \"avatarUrl\": \"https://cdn.bookhub/new-avatar.png\"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.displayName").value("Nicolas Bon"));

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.displayName").value("Nicolas Bon"))
        .andExpect(jsonPath("$.bio").value("Updated bio"))
        .andExpect(jsonPath("$.avatarUrl").value("https://cdn.bookhub/new-avatar.png"));
  }

  @Test
  @DisplayName("Should never expose sensitive fields in public profile endpoint")
  void shouldNeverExposeSensitiveFieldsInPublicProfileEndpoint() throws Exception {
    final User savedUser =
        userJpaRepository.save(
            User.create("nico", "nico@example.com", "ignored", "Nico", UserRole.USER));
    savedUser.updateProfile("Nico", "Public bio", "https://cdn.bookhub/public.png");
    userJpaRepository.save(savedUser);

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
        .perform(
            get("/api/v1/users/" + savedUser.getId()).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(savedUser.getId().toString()))
        .andExpect(jsonPath("$.username").value("nico"))
        .andExpect(jsonPath("$.displayName").value("Nico"))
        .andExpect(jsonPath("$.bio").value("Public bio"))
        .andExpect(jsonPath("$.avatarUrl").value("https://cdn.bookhub/public.png"))
        .andExpect(jsonPath("$.email").doesNotExist())
        .andExpect(jsonPath("$.role").doesNotExist())
        .andExpect(jsonPath("$.passwordHash").doesNotExist())
        .andExpect(jsonPath("$.status").doesNotExist());
  }

  @ParameterizedTest(name = "Should reject forbidden profile field mutation for {0}")
  @MethodSource("forbiddenProfileFields")
  void shouldRejectForbiddenProfileFieldMutationAndKeepPersistenceUnchanged(
      final String fieldName, final String fieldValueLiteral) throws Exception {
    final User savedUser =
        userJpaRepository.save(
            User.create("nico", "nico@example.com", "ignored", "Nico", UserRole.USER));
    savedUser.updateProfile("Nico", "Original bio", "https://cdn.bookhub/original.png");
    userJpaRepository.save(savedUser);

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
        .perform(
            patch("/api/v1/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      \"displayName\": \"Nico Updated\",
                      \"%s\": %s
                    }
                    """
                        .formatted(fieldName, fieldValueLiteral)))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.displayName").value("Nico"))
        .andExpect(jsonPath("$.bio").value("Original bio"))
        .andExpect(jsonPath("$.avatarUrl").value("https://cdn.bookhub/original.png"))
        .andExpect(jsonPath("$.email").value("nico@example.com"))
        .andExpect(jsonPath("$.role").value("USER"));
  }

  private static Stream<Arguments> forbiddenProfileFields() {
    return Stream.of(
        Arguments.of("email", "\"attacker@example.com\""),
        Arguments.of("username", "\"hacker\""),
        Arguments.of("role", "\"ADMIN\""),
        Arguments.of("status", "\"INACTIVE\""),
        Arguments.of("credentials", "\"changed\""));
  }
}
