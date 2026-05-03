package com.bookhub.library.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, UUID> {

  List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
