package com.bookhub.identity.web.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.application.admin.ChangeUserRoleService;
import com.bookhub.identity.application.admin.ListUsersService;
import com.bookhub.identity.application.auth.ratelimit.AuthRateLimitStore;
import com.bookhub.identity.application.auth.ratelimit.RateLimitDecision;
import com.bookhub.identity.config.JwtKeyConfig;
import com.bookhub.identity.config.SecurityConfig;
import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserNotFoundException;
import com.bookhub.identity.domain.user.UserRole;
import com.bookhub.identity.web.JwtTestTokenFactory;
import com.bookhub.identity.web.error.GlobalExceptionHandler;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminUserController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtKeyConfig.class})
@ActiveProfiles("test")
class AdminUserControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtEncoder jwtEncoder;

  @MockitoBean private ListUsersService listUsersService;
  @MockitoBean private ChangeUserRoleService changeUserRoleService;
  @MockitoBean private AuthRateLimitStore authRateLimitStore;
  @MockitoBean private Clock clock;

  @BeforeEach
  void setUp() {
    when(clock.instant()).thenReturn(Instant.parse("2026-05-09T12:00:00Z"));
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    when(authRateLimitStore.consume(any(), anyInt(), any(Duration.class)))
        .thenReturn(RateLimitDecision.allowed(99, Duration.ofSeconds(60)));
  }

  // -------------------------------------------------------------------------
  // Authentication / authorization boundary tests
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Should return 401 when listing users without authentication")
  void shouldReturn401WhenListingUsersWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/admin/users")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should return 403 when listing users with USER role")
  void shouldReturn403WhenListingUsersWithUserRole() throws Exception {
    final String token = buildToken("usr_001", "USER");

    mockMvc
        .perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Should return 401 when changing role without authentication")
  void shouldReturn401WhenChangingRoleWithoutAuthentication() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/admin/users/" + UUID.randomUUID() + "/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\": \"ADMIN\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should return 403 when changing role with USER role")
  void shouldReturn403WhenChangingRoleWithUserRole() throws Exception {
    final String token = buildToken("usr_001", "USER");

    mockMvc
        .perform(
            patch("/api/v1/admin/users/" + UUID.randomUUID() + "/role")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\": \"ADMIN\"}"))
        .andExpect(status().isForbidden());
  }

  // -------------------------------------------------------------------------
  // List users — happy paths
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Should list users with default pagination when authenticated as ADMIN")
  void shouldListUsersWithDefaultPaginationWhenAuthenticatedAsAdmin() throws Exception {
    final String token = buildToken("usr_admin", "ADMIN");
    final UUID fixedId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    final User user = buildUser(fixedId.toString(), "nico", UserRole.USER);

    when(listUsersService.list(eq(0), eq(20), isNull()))
        .thenReturn(new ListUsersService.PagedUsersResult(List.of(user), 0, 20, 1L, 1L));

    mockMvc
        .perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[0].userId").value(fixedId.toString()))
        .andExpect(jsonPath("$.items[0].username").value("nico"))
        .andExpect(jsonPath("$.items[0].role").value("USER"))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.totalPages").value(1));
  }

  @Test
  @DisplayName("Should filter users by role when role param is provided")
  void shouldFilterUsersByRoleWhenRoleParamIsProvided() throws Exception {
    final String token = buildToken("usr_admin", "ADMIN");
    final User adminUser = buildUser("usr_002", "admin_nico", UserRole.ADMIN);

    when(listUsersService.list(eq(0), eq(20), eq(UserRole.ADMIN)))
        .thenReturn(new ListUsersService.PagedUsersResult(List.of(adminUser), 0, 20, 1L, 1L));

    mockMvc
        .perform(
            get("/api/v1/admin/users?role=ADMIN").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].role").value("ADMIN"));
  }

  @Test
  @DisplayName("Should return 400 when invalid role filter is provided")
  void shouldReturn400WhenInvalidRoleFilterIsProvided() throws Exception {
    final String token = buildToken("usr_admin", "ADMIN");

    mockMvc
        .perform(
            get("/api/v1/admin/users?role=SUPERADMIN").header("Authorization", "Bearer " + token))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  // -------------------------------------------------------------------------
  // Change role — happy paths
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("Should change user role and return updated user when authenticated as ADMIN")
  void shouldChangeUserRoleAndReturnUpdatedUserWhenAuthenticatedAsAdmin() throws Exception {
    final String token = buildToken("usr_admin", "ADMIN");
    final UUID targetUserId = UUID.randomUUID();
    final User updated = buildUser(targetUserId.toString(), "nico", UserRole.ADMIN);

    when(changeUserRoleService.changeRole(eq(targetUserId), eq(UserRole.ADMIN)))
        .thenReturn(updated);

    mockMvc
        .perform(
            patch("/api/v1/admin/users/" + targetUserId + "/role")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\": \"ADMIN\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(targetUserId.toString()))
        .andExpect(jsonPath("$.role").value("ADMIN"));
  }

  @Test
  @DisplayName("Should return 404 when user to change role is not found")
  void shouldReturn404WhenUserToChangeRoleIsNotFound() throws Exception {
    final String token = buildToken("usr_admin", "ADMIN");
    final UUID unknownId = UUID.randomUUID();

    when(changeUserRoleService.changeRole(eq(unknownId), any()))
        .thenThrow(new UserNotFoundException(unknownId.toString()));

    mockMvc
        .perform(
            patch("/api/v1/admin/users/" + unknownId + "/role")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\": \"ADMIN\"}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
  }

  @Test
  @DisplayName("Should return 400 when role value is invalid in change role request")
  void shouldReturn400WhenRoleValueIsInvalidInChangeRoleRequest() throws Exception {
    final String token = buildToken("usr_admin", "ADMIN");

    mockMvc
        .perform(
            patch("/api/v1/admin/users/" + UUID.randomUUID() + "/role")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\": \"SUPERADMIN\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  @DisplayName("Should return 400 when role field is missing in change role request")
  void shouldReturn400WhenRoleFieldIsMissingInChangeRoleRequest() throws Exception {
    final String token = buildToken("usr_admin", "ADMIN");

    mockMvc
        .perform(
            patch("/api/v1/admin/users/" + UUID.randomUUID() + "/role")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private String buildToken(final String userId, final String role) {
    final Instant now = Instant.now();
    return JwtTestTokenFactory.createAccessToken(
        jwtEncoder, userId, "nico", "Nicolas", role, "nico@example.com",
        now, now.plusSeconds(3600));
  }

  private User buildUser(final String userId, final String username, final UserRole role) {
    final UUID id = userId.length() == 36 ? UUID.fromString(userId) : UUID.randomUUID();
    return User.rehydrate(id, username, username + "@example.com", "hashed", username, role);
  }
}
