package com.bookhub.library.application.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhub.library.domain.PaginatedResult;
import com.bookhub.library.domain.PaginationQuery;
import com.bookhub.library.domain.Review;
import com.bookhub.library.domain.ReviewRepository;
import com.bookhub.library.domain.ReviewStatus;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListReviewsForModerationServiceTest {

  @Mock private ReviewRepository reviewRepository;

  @InjectMocks private ListReviewsForModerationService listReviewsService;

  private static final UUID REVIEW_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

  @Nested
  class ListAll {

    @Test
    void shouldReturnPaginatedReviewsWithoutFilter() {
      final Review review = sampleReview(ReviewStatus.VISIBLE);
      final PaginatedResult<Review> expected = new PaginatedResult<>(List.of(review), 0, 20, 1, 1);

      when(reviewRepository.findAllForModeration(any(PaginationQuery.class))).thenReturn(expected);

      final PaginatedResult<Review> result = listReviewsService.list(0, 20, null);

      assertThat(result.items()).hasSize(1);
      assertThat(result.totalElements()).isEqualTo(1);
      verify(reviewRepository).findAllForModeration(any(PaginationQuery.class));
    }

    @Test
    void shouldFilterByStatusWhenProvided() {
      final Review hiddenReview = sampleReview(ReviewStatus.HIDDEN);
      final PaginatedResult<Review> expected =
          new PaginatedResult<>(List.of(hiddenReview), 0, 20, 1, 1);

      when(reviewRepository.findAllForModeration(
              any(PaginationQuery.class), eq(ReviewStatus.HIDDEN)))
          .thenReturn(expected);

      final PaginatedResult<Review> result = listReviewsService.list(0, 20, ReviewStatus.HIDDEN);

      assertThat(result.items().getFirst().getStatus()).isEqualTo(ReviewStatus.HIDDEN);
      verify(reviewRepository)
          .findAllForModeration(any(PaginationQuery.class), eq(ReviewStatus.HIDDEN));
    }

    @Test
    void shouldReturnEmptyPageWhenNoReviews() {
      final PaginatedResult<Review> empty = new PaginatedResult<>(List.of(), 0, 20, 0, 0);

      when(reviewRepository.findAllForModeration(any(PaginationQuery.class))).thenReturn(empty);

      final PaginatedResult<Review> result = listReviewsService.list(0, 20, null);

      assertThat(result.items()).isEmpty();
      assertThat(result.totalElements()).isEqualTo(0);
    }
  }

  private Review sampleReview(final ReviewStatus status) {
    return Review.rehydrateBuilder()
        .id(REVIEW_ID)
        .userId(USER_ID)
        .bookId(BOOK_ID)
        .rating(4)
        .content("Sample")
        .status(status)
        .createdAt(java.time.Instant.now())
        .updatedAt(java.time.Instant.now())
        .build();
  }
}
