package com.bookhub.library.application;

import com.bookhub.library.domain.Notification;
import com.bookhub.library.domain.NotificationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetNotificationService {

  private final NotificationRepository notificationRepository;

  public List<Notification> forUser(final UUID userId) {
    return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }
}
