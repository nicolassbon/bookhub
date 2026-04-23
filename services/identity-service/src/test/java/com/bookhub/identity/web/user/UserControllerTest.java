package com.bookhub.identity.web.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class UserControllerTest {

  private final UserController userController = new UserController();

  @Test
  @DisplayName("Should map authenticated JWT claims to user profile response")
  void shouldMapAuthenticatedJwtClaimsToUserProfileResponse() {
    final Jwt jwt =
        new Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"),
            Map.of(
                "sub", "user-123",
                "username", "nico",
                "displayName", "Nicolas Bon",
                "email", "nico@example.com",
                "role", "USER"));

    final JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, List.of());

    final UserProfileResponse response = userController.me(authentication);

    assertThat(response.userId()).isEqualTo("user-123");
    assertThat(response.username()).isEqualTo("nico");
    assertThat(response.displayName()).isEqualTo("Nicolas Bon");
    assertThat(response.email()).isEqualTo("nico@example.com");
    assertThat(response.role()).isEqualTo("USER");
  }
}
