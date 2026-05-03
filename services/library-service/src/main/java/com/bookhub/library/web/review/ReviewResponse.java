package com.bookhub.library.web.review;

import com.bookhub.library.domain.Review;
import com.bookhub.library.domain.ReviewStatus;
import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
    UUID id,
    UUID userId,
    UUID bookId,
    int rating,
    String content,
    ReviewStatus status,
    Instant createdAt,
    Instant updatedAt) {

  public static ReviewResponse from(final Review review) {
    return new ReviewResponse(
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
