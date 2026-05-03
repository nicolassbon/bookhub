package com.bookhub.library.application;

import com.bookhub.library.application.error.NotificationNotFoundException;
import com.bookhub.library.application.error.NotificationOwnershipException;
import com.bookhub.library.domain.Notification;
import com.bookhub.library.domain.NotificationRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkNotificationAsReadService {

  private final NotificationRepository notificationRepository;

  @Transactional
  public void execute(final UUID userId, final UUID notificationId) {
    final Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(
                () ->
                    new NotificationNotFoundException(
                        "Notification " + notificationId + " not found"));

    if (!notification.isOwnedBy(userId)) {
      throw new NotificationOwnershipException("User does not own notification " + notificationId);
    }

    notification.markAsRead();
    notificationRepository.save(notification);
  }
}
