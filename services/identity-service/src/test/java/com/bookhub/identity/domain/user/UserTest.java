package com.bookhub.identity.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  @DisplayName("Should update displayName, bio and avatarUrl")
  void shouldUpdateDisplayNameBioAndAvatarUrl() {
    final User user =
        User.rehydrate(
            UUID.randomUUID(),
            "nico",
            "nico@example.com",
            "password-hash",
            "Nico",
            null,
            null,
            UserRole.USER);

    user.updateProfile("Nicolas Bon", "Backend engineer", "https://cdn.bookhub/avatar.png");

    assertThat(user.getDisplayName()).isEqualTo("Nicolas Bon");
    assertThat(user.getBio()).isEqualTo("Backend engineer");
    assertThat(user.getAvatarUrl()).isEqualTo("https://cdn.bookhub/avatar.png");
  }

  @Test
  @DisplayName("Should allow clearing optional bio and avatarUrl with null values")
  void shouldAllowClearingOptionalBioAndAvatarUrlWithNullValues() {
    final User user =
        User.rehydrate(
            UUID.randomUUID(),
            "nico",
            "nico@example.com",
            "password-hash",
            "Nico",
            "Old bio",
            "https://cdn.bookhub/old-avatar.png",
            UserRole.USER);

    user.updateProfile("Nico", null, null);

    assertThat(user.getDisplayName()).isEqualTo("Nico");
    assertThat(user.getBio()).isNull();
    assertThat(user.getAvatarUrl()).isNull();
  }
}
