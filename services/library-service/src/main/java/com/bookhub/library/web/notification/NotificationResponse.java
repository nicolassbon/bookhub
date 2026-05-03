package com.bookhub.library.web.notification;

import com.bookhub.library.domain.Notification;
import com.bookhub.library.domain.NotificationStatus;
import com.bookhub.library.domain.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    NotificationType type,
    String title,
    String message,
    String payload,
    NotificationStatus status,
    Instant createdAt,
    Instant readAt) {

  public static NotificationResponse from(final Notification notification) {
    return new NotificationResponse(
        notification.getId(),
        notification.getType(),
        notification.getTitle(),
        notification.getMessage(),
        notification.getPayload(),
        notification.getStatus(),
        notification.getCreatedAt(),
        notification.getReadAt());
  }
}
