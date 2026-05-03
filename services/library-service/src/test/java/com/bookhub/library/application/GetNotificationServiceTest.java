package com.bookhub.library.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.bookhub.library.domain.Notification;
import com.bookhub.library.domain.NotificationRepository;
import com.bookhub.library.domain.NotificationType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetNotificationServiceTest {

  @Mock private NotificationRepository notificationRepository;

  @InjectMocks private GetNotificationService getNotificationService;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Nested
  class ForUser {

    @Test
    void shouldReturnNotificationsForUser() {
      final Notification notif =
          Notification.create(USER_ID, NotificationType.SYSTEM, "Title", "Message", null);
      when(notificationRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
          .thenReturn(List.of(notif));

      final List<Notification> result = getNotificationService.forUser(USER_ID);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst()).isEqualTo(notif);
    }
  }
}
