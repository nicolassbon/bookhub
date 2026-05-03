package com.bookhub.library.infrastructure.persistence;

import com.bookhub.library.domain.ReviewStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaReviewRepository extends JpaRepository<ReviewEntity, UUID> {

  Optional<ReviewEntity> findByUserIdAndBookId(UUID userId, UUID bookId);

  List<ReviewEntity> findByBookId(UUID bookId);

  List<ReviewEntity> findByBookIdAndStatus(UUID bookId, ReviewStatus status);
}
