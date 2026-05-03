package com.bookhub.library.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NotificationTest {

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Nested
  class Create {

    @Test
    void shouldCreateNotification() {
      final Notification notif =
          Notification.create(
              USER_ID, NotificationType.GOAL_ACHIEVED, "Goal Met!", "Goal reached!", "{}");

      assertThat(notif.getUserId()).isEqualTo(USER_ID);
      assertThat(notif.getType()).isEqualTo(NotificationType.GOAL_ACHIEVED);
      assertThat(notif.getTitle()).isEqualTo("Goal Met!");
      assertThat(notif.getMessage()).isEqualTo("Goal reached!");
      assertThat(notif.getPayload()).isEqualTo("{}");
      assertThat(notif.getStatus()).isEqualTo(NotificationStatus.UNREAD);
      assertThat(notif.getCreatedAt()).isNotNull();
      assertThat(notif.getReadAt()).isNull();
    }

    @Test
    void shouldRejectNullUserId() {
      assertThatThrownBy(
              () -> Notification.create(null, NotificationType.SYSTEM, "Title", "Message", null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullType() {
      assertThatThrownBy(() -> Notification.create(USER_ID, null, "Title", "Message", null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullTitle() {
      assertThatThrownBy(
              () -> Notification.create(USER_ID, NotificationType.SYSTEM, null, "Message", null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  class MarkAsRead {

    @Test
    void shouldMarkAsReadWhenUnread() {
      final Notification notif =
          Notification.create(USER_ID, NotificationType.SYSTEM, "Title", "Hello", null);

      notif.markAsRead();

      assertThat(notif.getStatus()).isEqualTo(NotificationStatus.READ);
      assertThat(notif.getReadAt()).isNotNull();
    }

    @Test
    void shouldDoNothingWhenAlreadyRead() {
      final Notification notif =
          Notification.create(USER_ID, NotificationType.SYSTEM, "Title", "Hello", null);
      notif.markAsRead();
      final var originalReadAt = notif.getReadAt();

      notif.markAsRead();

      assertThat(notif.getStatus()).isEqualTo(NotificationStatus.READ);
      assertThat(notif.getReadAt()).isEqualTo(originalReadAt);
    }
  }

  @Nested
  class IsOwnedBy {

    @Test
    void shouldReturnTrueForOwner() {
      final Notification notif =
          Notification.create(USER_ID, NotificationType.SYSTEM, "Title", "Hello", null);
      assertThat(notif.isOwnedBy(USER_ID)).isTrue();
    }

    @Test
    void shouldReturnFalseForOtherUser() {
      final Notification notif =
          Notification.create(USER_ID, NotificationType.SYSTEM, "Title", "Hello", null);
      final UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000099");
      assertThat(notif.isOwnedBy(otherUserId)).isFalse();
    }
  }
}
