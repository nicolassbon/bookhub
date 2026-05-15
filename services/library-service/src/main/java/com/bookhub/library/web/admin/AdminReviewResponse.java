package com.bookhub.library.web.admin;

import com.bookhub.library.domain.Review;
import com.bookhub.library.domain.ReviewStatus;
import java.time.Instant;
import java.util.UUID;

public record AdminReviewResponse(
    UUID reviewId,
    UUID userId,
    UUID bookId,
    int rating,
    String content,
    ReviewStatus status,
    Instant createdAt,
    Instant updatedAt) {

  public static AdminReviewResponse from(final Review review) {
    return new AdminReviewResponse(
        review.getId(),
        review.getUserId(),
        review.getBookId(),
        review.getRating(),
        review.getContent(),
        review.getStatus(),
        review.getCreatedAt(),
        review.getUpdatedAt());
  }
}
