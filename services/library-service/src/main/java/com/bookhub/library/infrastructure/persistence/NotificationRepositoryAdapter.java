package com.bookhub.library.infrastructure.persistence;

import com.bookhub.library.domain.Notification;
import com.bookhub.library.domain.NotificationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

  private final JpaNotificationRepository jpaRepository;
  private final NotificationEntityMapper mapper;

  @Override
  public Notification save(final Notification notification) {
    final NotificationEntity entity = mapper.toEntity(notification);
    final NotificationEntity saved = jpaRepository.save(entity);
    notification.setId(saved.getId());
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Notification> findById(final UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Notification> findByUserIdOrderByCreatedAtDesc(final UUID userId) {
    return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
