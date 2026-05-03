package com.bookhub.library.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhub.library.domain.Notification;
import com.bookhub.library.domain.NotificationRepository;
import com.bookhub.library.domain.NotificationType;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateNotificationServiceTest {

  @Mock private NotificationRepository notificationRepository;

  @InjectMocks private CreateNotificationService createNotificationService;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Nested
  class Execute {

    @Test
    void shouldCreateAndSaveNotification() {
      // Arrange
      when(notificationRepository.save(any(Notification.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      final Notification notif =
          createNotificationService.execute(
              USER_ID, NotificationType.GOAL_ACHIEVED, "Goal Met", "Congrats!", "{\"year\":2026}");

      // Assert
      assertThat(notif.getUserId()).isEqualTo(USER_ID);
      assertThat(notif.getType()).isEqualTo(NotificationType.GOAL_ACHIEVED);
      assertThat(notif.getTitle()).isEqualTo("Goal Met");
      assertThat(notif.getMessage()).isEqualTo("Congrats!");
      assertThat(notif.getPayload()).isEqualTo("{\"year\":2026}");

      final ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
      verify(notificationRepository).save(captor.capture());
      assertThat(captor.getValue().getTitle()).isEqualTo("Goal Met");
    }
  }
}
