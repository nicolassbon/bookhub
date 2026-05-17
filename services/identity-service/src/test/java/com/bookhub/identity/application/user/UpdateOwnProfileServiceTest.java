package com.bookhub.identity.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserNotFoundException;
import com.bookhub.identity.domain.user.UserRepository;
import com.bookhub.identity.domain.user.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateOwnProfileServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UpdateOwnProfileService updateOwnProfileService;

  @Test
  @DisplayName("Should update only provided editable fields")
  void shouldUpdateOnlyProvidedEditableFields() {
    final UUID userId = UUID.randomUUID();
    final User user =
        User.rehydrate(
            userId,
            "nico",
            "nico@example.com",
            "hash",
            "Nico",
            "Old bio",
            "https://cdn.bookhub/old.png",
            UserRole.USER);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    final User result =
        updateOwnProfileService.execute(
            userId.toString(),
            UpdateOwnProfileCommand.builder()
                .displayName("Nicolas Bon")
                .bio("New bio")
                .bioProvided(true)
                .avatarUrlProvided(false)
                .build());

    assertThat(result.getDisplayName()).isEqualTo("Nicolas Bon");
    assertThat(result.getBio()).isEqualTo("New bio");
    assertThat(result.getAvatarUrl()).isEqualTo("https://cdn.bookhub/old.png");
  }

  @Test
  @DisplayName("Should reject out-of-scope field mutation")
  void shouldRejectOutOfScopeFieldMutation() {
    final UUID userId = UUID.randomUUID();

    assertThatThrownBy(
            () ->
                updateOwnProfileService.execute(
                    userId.toString(),
                    UpdateOwnProfileCommand.builder().displayName("Nico").email("x@y.com").build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Only displayName, bio and avatarUrl can be updated");

    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw user not found when subject does not exist")
  void shouldThrowUserNotFoundWhenSubjectDoesNotExist() {
    final UUID userId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                updateOwnProfileService.execute(
                    userId.toString(), UpdateOwnProfileCommand.builder().build()))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage("User not found: " + userId);
  }
}
