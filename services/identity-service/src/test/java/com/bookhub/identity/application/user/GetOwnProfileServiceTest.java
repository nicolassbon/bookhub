package com.bookhub.identity.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
class GetOwnProfileServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private GetOwnProfileService getOwnProfileService;

  @Test
  @DisplayName("Should load persisted user by authenticated subject")
  void shouldLoadPersistedUserByAuthenticatedSubject() {
    final UUID userId = UUID.randomUUID();
    final User user =
        User.rehydrate(userId, "nico", "nico@example.com", "hash", "Nico", UserRole.USER);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    final User result = getOwnProfileService.execute(userId.toString());

    assertThat(result.getId()).isEqualTo(userId);
    assertThat(result.getUsername()).isEqualTo("nico");
  }

  @Test
  @DisplayName("Should throw user not found when subject does not exist")
  void shouldThrowUserNotFoundWhenSubjectDoesNotExist() {
    final UUID userId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> getOwnProfileService.execute(userId.toString()))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage("User not found: " + userId);
  }
}
