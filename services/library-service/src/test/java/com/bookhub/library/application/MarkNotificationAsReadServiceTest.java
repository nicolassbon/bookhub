package com.bookhub.library.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhub.library.application.error.NotificationNotFoundException;
import com.bookhub.library.application.error.NotificationOwnershipException;
import com.bookhub.library.domain.Notification;
import com.bookhub.library.domain.NotificationRepository;
import com.bookhub.library.domain.NotificationStatus;
import com.bookhub.library.domain.NotificationType;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarkNotificationAsReadServiceTest {

  @Mock private NotificationRepository notificationRepository;

  @InjectMocks private MarkNotificationAsReadService markNotificationAsReadService;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");
  private static final UUID NOTIF_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Nested
  class Execute {

    @Test
    void shouldMarkAsRead() {
      final Notification notif =
          Notification.create(USER_ID, NotificationType.SYSTEM, "Title", "Message", null);
      notif.setId(NOTIF_ID);

      when(notificationRepository.findById(NOTIF_ID)).thenReturn(Optional.of(notif));
      when(notificationRepository.save(any(Notification.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      markNotificationAsReadService.execute(USER_ID, NOTIF_ID);

      assertThat(notif.getStatus()).isEqualTo(NotificationStatus.READ);
      verify(notificationRepository).save(notif);
    }

    @Test
    void shouldThrowNotFoundWhenNotificationDoesNotExist() {
      when(notificationRepository.findById(NOTIF_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> markNotificationAsReadService.execute(USER_ID, NOTIF_ID))
          .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void shouldThrowOwnershipExceptionWhenUserIsNotOwner() {
      final Notification notif =
          Notification.create(USER_ID, NotificationType.SYSTEM, "Title", "Message", null);
      notif.setId(NOTIF_ID);

      when(notificationRepository.findById(NOTIF_ID)).thenReturn(Optional.of(notif));

      assertThatThrownBy(() -> markNotificationAsReadService.execute(OTHER_USER_ID, NOTIF_ID))
          .isInstanceOf(NotificationOwnershipException.class);
    }
  }
}
