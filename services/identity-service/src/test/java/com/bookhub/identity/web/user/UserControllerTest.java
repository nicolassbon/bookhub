package com.bookhub.identity.web.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.application.auth.ratelimit.AuthRateLimitStore;
import com.bookhub.identity.application.user.GetOwnProfileService;
import com.bookhub.identity.application.user.GetPublicProfileService;
import com.bookhub.identity.application.user.UpdateOwnProfileService;
import com.bookhub.identity.config.JwtKeyConfig;
import com.bookhub.identity.config.SecurityConfig;
import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserNotFoundException;
import com.bookhub.identity.domain.user.UserRole;
import com.bookhub.identity.web.error.GlobalExceptionHandler;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtKeyConfig.class})
@ActiveProfiles("test")
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetOwnProfileService getOwnProfileService;

  @MockitoBean private GetPublicProfileService getPublicProfileService;

  @MockitoBean private UpdateOwnProfileService updateOwnProfileService;

  @MockitoBean private AuthRateLimitStore authRateLimitStore;

  @MockitoBean private JwtDecoder jwtDecoder;

  @BeforeEach
  void setUp() {
    final Jwt jwt =
        new Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"),
            Map.of(
                "sub", "6676f2d8-0f65-40ae-b102-66145e24f3fd",
                "role", "USER",
                "username", "nico",
                "email", "nico@example.com",
                "displayName", "Nico"));
    when(jwtDecoder.decode(anyString())).thenReturn(jwt);
  }

  @Test
  @DisplayName("Should return persisted self profile for authenticated user")
  void shouldReturnPersistedSelfProfileForAuthenticatedUser() throws Exception {
    final User user =
        User.rehydrate(
            UUID.fromString("6676f2d8-0f65-40ae-b102-66145e24f3fd"),
            "nico",
            "nico@example.com",
            "hash",
            "Nicolas Bon",
            "Backend engineer",
            "https://cdn.bookhub/avatar.png",
            UserRole.USER);

    when(getOwnProfileService.execute(anyString())).thenReturn(user);

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value("6676f2d8-0f65-40ae-b102-66145e24f3fd"))
        .andExpect(jsonPath("$.username").value("nico"))
        .andExpect(jsonPath("$.displayName").value("Nicolas Bon"))
        .andExpect(jsonPath("$.email").value("nico@example.com"))
        .andExpect(jsonPath("$.role").value("USER"))
        .andExpect(jsonPath("$.bio").value("Backend engineer"))
        .andExpect(jsonPath("$.avatarUrl").value("https://cdn.bookhub/avatar.png"));
  }

  @Test
  @DisplayName("Should return public profile without sensitive fields")
  void shouldReturnPublicProfileWithoutSensitiveFields() throws Exception {
    final User user =
        User.rehydrate(
            UUID.fromString("6676f2d8-0f65-40ae-b102-66145e24f3fd"),
            "nico",
            "nico@example.com",
            "hash",
            "Nicolas Bon",
            "Backend engineer",
            "https://cdn.bookhub/avatar.png",
            UserRole.USER);

    when(getPublicProfileService.execute(any(UUID.class))).thenReturn(user);

    mockMvc
        .perform(
            get("/api/v1/users/6676f2d8-0f65-40ae-b102-66145e24f3fd")
                .header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value("6676f2d8-0f65-40ae-b102-66145e24f3fd"))
        .andExpect(jsonPath("$.username").value("nico"))
        .andExpect(jsonPath("$.displayName").value("Nicolas Bon"))
        .andExpect(jsonPath("$.bio").value("Backend engineer"))
        .andExpect(jsonPath("$.avatarUrl").value("https://cdn.bookhub/avatar.png"))
        .andExpect(jsonPath("$.email").doesNotExist())
        .andExpect(jsonPath("$.role").doesNotExist());
  }

  @Test
  @DisplayName("Should return 200 when owner updates profile")
  void shouldReturn200WhenOwnerUpdatesProfile() throws Exception {
    final User user =
        User.rehydrate(
            UUID.fromString("6676f2d8-0f65-40ae-b102-66145e24f3fd"),
            "nico",
            "nico@example.com",
            "hash",
            "Nicolas Updated",
            "New bio",
            "https://cdn.bookhub/new-avatar.png",
            UserRole.USER);
    when(updateOwnProfileService.execute(anyString(), any())).thenReturn(user);

    mockMvc
        .perform(
            patch("/api/v1/users/me")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      \"displayName\": \"Nicolas Updated\",
                      \"bio\": \"New bio\",
                      \"avatarUrl\": \"https://cdn.bookhub/new-avatar.png\"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.displayName").value("Nicolas Updated"))
        .andExpect(jsonPath("$.bio").value("New bio"));
  }

  @Test
  @DisplayName("Should return 400 with validation error for invalid payload")
  void shouldReturn400WithValidationErrorForInvalidPayload() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/users/me")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      \"displayName\": \"\",
                      \"avatarUrl\": \"not-an-url\"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  @DisplayName("Should return 404 when public profile is not found")
  void shouldReturn404WhenPublicProfileIsNotFound() throws Exception {
    when(getPublicProfileService.execute(any(UUID.class)))
        .thenThrow(new UserNotFoundException("6676f2d8-0f65-40ae-b102-66145e24f3fd"));

    mockMvc
        .perform(
            get("/api/v1/users/6676f2d8-0f65-40ae-b102-66145e24f3fd")
                .header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
  }

  @Test
  @DisplayName("Should return 401 for unauthenticated requests")
  void shouldReturn401ForUnauthenticatedRequests() throws Exception {
    mockMvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should return 401 for unauthenticated patch profile requests")
  void shouldReturn401ForUnauthenticatedPatchProfileRequests() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      \"displayName\": \"Nico\"
                    }
                    """))
        .andExpect(status().isUnauthorized());
  }
}
