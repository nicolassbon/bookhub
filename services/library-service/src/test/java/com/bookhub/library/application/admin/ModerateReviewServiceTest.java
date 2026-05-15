package com.bookhub.library.application.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhub.library.application.error.ReviewNotFoundException;
import com.bookhub.library.domain.PaginatedResult;
import com.bookhub.library.domain.PaginationQuery;
import com.bookhub.library.domain.Review;
import com.bookhub.library.domain.ReviewRepository;
import com.bookhub.library.domain.ReviewStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModerateReviewServiceTest {

  @Mock private ReviewRepository reviewRepository;

  @InjectMocks private ModerateReviewService moderateReviewService;

  private static final UUID REVIEW_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID USER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID BOOK_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000003");

  @Nested
  class Moderate {

    @Test
    void shouldApproveReview() {
      final Review review =
          Review.rehydrateBuilder()
              .id(REVIEW_ID)
              .userId(USER_ID)
              .bookId(BOOK_ID)
              .rating(4)
              .content("Good book")
              .status(ReviewStatus.FLAGGED)
              .createdAt(java.time.Instant.now())
              .updatedAt(java.time.Instant.now())
              .build();

      when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
      when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

      final Review result = moderateReviewService.moderate(REVIEW_ID, ReviewStatus.VISIBLE);

      assertThat(result.getStatus()).isEqualTo(ReviewStatus.VISIBLE);
      verify(reviewRepository).save(review);
    }

    @Test
    void shouldRejectReview() {
      final Review review =
          Review.rehydrateBuilder()
              .id(REVIEW_ID)
              .userId(USER_ID)
              .bookId(BOOK_ID)
              .rating(1)
              .content("Spam")
              .status(ReviewStatus.FLAGGED)
              .createdAt(java.time.Instant.now())
              .updatedAt(java.time.Instant.now())
              .build();

      when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
      when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

      final Review result = moderateReviewService.moderate(REVIEW_ID, ReviewStatus.HIDDEN);

      assertThat(result.getStatus()).isEqualTo(ReviewStatus.HIDDEN);
    }

    @Test
    void shouldFlagReview() {
      final Review review =
          Review.rehydrateBuilder()
              .id(REVIEW_ID)
              .userId(USER_ID)
              .bookId(BOOK_ID)
              .rating(3)
              .content("Suspicious")
              .status(ReviewStatus.VISIBLE)
              .createdAt(java.time.Instant.now())
              .updatedAt(java.time.Instant.now())
              .build();

      when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
      when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

      final Review result = moderateReviewService.moderate(REVIEW_ID, ReviewStatus.FLAGGED);

      assertThat(result.getStatus()).isEqualTo(ReviewStatus.FLAGGED);
    }

    @Test
    void shouldThrowWhenReviewNotFound() {
      when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> moderateReviewService.moderate(REVIEW_ID, ReviewStatus.VISIBLE))
          .isInstanceOf(ReviewNotFoundException.class)
          .hasMessageContaining(REVIEW_ID.toString());
    }
  }
}
