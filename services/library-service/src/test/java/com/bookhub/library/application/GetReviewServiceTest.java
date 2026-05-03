package com.bookhub.library.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
class GetReviewServiceTest {

  @Mock private ReviewRepository reviewRepository;

  @InjectMocks private GetReviewService getReviewService;

  private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Nested
  class ForBook {

    @Test
    void shouldReturnReviewsForBook() {
      final UUID user1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
      final UUID user2 = UUID.fromString("00000000-0000-0000-0000-000000000003");
      final Review review1 = Review.create(user1, BOOK_ID, 5, "Amazing!");
      final Review review2 = Review.create(user2, BOOK_ID, 4, "Good");

      when(reviewRepository.findByBookIdAndStatus(BOOK_ID, ReviewStatus.VISIBLE))
          .thenReturn(List.of(review1, review2));

      final List<Review> reviews = getReviewService.forBook(BOOK_ID);

      assertThat(reviews).hasSize(2);
      assertThat(reviews).containsExactly(review1, review2);
    }
  }
}
