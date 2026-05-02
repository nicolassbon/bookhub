package com.bookhub.library.infrastructure.persistence;

import com.bookhub.library.domain.ReadingState;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserBookRepository extends JpaRepository<UserBookEntity, UUID> {

  Optional<UserBookEntity> findByUserIdAndBookId(UUID userId, UUID bookId);

  List<UserBookEntity> findByUserId(UUID userId);

  List<UserBookEntity> findByUserIdAndState(UUID userId, ReadingState state);

  long countByUserId(UUID userId);

  long countByUserIdAndState(UUID userId, ReadingState state);
}
