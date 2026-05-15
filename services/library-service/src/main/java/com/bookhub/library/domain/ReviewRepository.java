package com.bookhub.library.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {

  Optional<Review> findByUserIdAndBookId(UUID userId, UUID bookId);

  List<Review> findByBookId(UUID bookId);

  List<Review> findByBookIdAndStatus(UUID bookId, ReviewStatus status);

  Optional<Review> findById(UUID reviewId);

  Review save(Review review);

  PaginatedResult<Review> findAllForModeration(PaginationQuery pagination);

  PaginatedResult<Review> findAllForModeration(PaginationQuery pagination, ReviewStatus status);
}
