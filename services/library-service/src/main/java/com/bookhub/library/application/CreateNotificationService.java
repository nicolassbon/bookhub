package com.bookhub.library.application;

import com.bookhub.library.domain.Notification;
import com.bookhub.library.domain.NotificationRepository;
import com.bookhub.library.domain.NotificationType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateNotificationService {

  private final NotificationRepository notificationRepository;

  @Transactional
  public Notification execute(
      final UUID userId,
      final NotificationType type,
      final String title,
      final String message,
      final String payload) {
    log.info("Creating notification for userId={} type={} title={}", userId, type, title);

    final Notification notification = Notification.create(userId, type, title, message, payload);
    return notificationRepository.save(notification);
  }
}
