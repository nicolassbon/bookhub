package com.bookhub.library.infrastructure.persistence;

import com.bookhub.library.domain.ReviewStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaReviewRepository extends JpaRepository<ReviewEntity, UUID> {

  Optional<ReviewEntity> findByUserIdAndBookId(UUID userId, UUID bookId);

  List<ReviewEntity> findByBookId(UUID bookId);

  List<ReviewEntity> findByBookIdAndStatus(UUID bookId, ReviewStatus status);

  Page<ReviewEntity> findByStatus(ReviewStatus status, Pageable pageable);

  long countByStatus(ReviewStatus status);

  @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM ReviewEntity r")
  double computeAverageRating();
}
