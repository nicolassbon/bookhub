package com.bookhub.library.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReviewTest {

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Nested
  class Create {

    @Test
    void shouldCreateReview() {
      final Review review = Review.create(USER_ID, BOOK_ID, 5, "Great book!");

      assertThat(review.getUserId()).isEqualTo(USER_ID);
      assertThat(review.getBookId()).isEqualTo(BOOK_ID);
      assertThat(review.getRating()).isEqualTo(5);
      assertThat(review.getContent()).isEqualTo("Great book!");
      assertThat(review.getStatus()).isEqualTo(ReviewStatus.VISIBLE);
      assertThat(review.getCreatedAt()).isNotNull();
      assertThat(review.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldCreateReviewWithoutContent() {
      final Review review = Review.create(USER_ID, BOOK_ID, 4, null);

      assertThat(review.getRating()).isEqualTo(4);
      assertThat(review.getContent()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6, -1, 10})
    void shouldRejectInvalidRating(final int rating) {
      assertThatThrownBy(() -> Review.create(USER_ID, BOOK_ID, rating, "Great!"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("rating");
    }

    @Test
    void shouldRejectNullUserId() {
      assertThatThrownBy(() -> Review.create(null, BOOK_ID, 5, "Great!"))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullBookId() {
      assertThatThrownBy(() -> Review.create(USER_ID, null, 5, "Great!"))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  class EditContent {

    @Test
    void shouldUpdateRatingAndContent() {
      final Review review = Review.create(USER_ID, BOOK_ID, 5, "Great!");

      review.editContent(4, "Good!");

      assertThat(review.getRating()).isEqualTo(4);
      assertThat(review.getContent()).isEqualTo("Good!");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6})
    void shouldRejectInvalidRatingOnEdit(final int rating) {
      final Review review = Review.create(USER_ID, BOOK_ID, 5, "Great!");

      assertThatThrownBy(() -> review.editContent(rating, "Updated"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("rating");
    }
  }

  @Nested
  class ChangeStatus {

    @Test
    void shouldUpdateStatus() {
      final Review review = Review.create(USER_ID, BOOK_ID, 5, "Great!");

      review.changeStatus(ReviewStatus.HIDDEN);

      assertThat(review.getStatus()).isEqualTo(ReviewStatus.HIDDEN);
    }

    @Test
    void shouldRejectNullStatus() {
      final Review review = Review.create(USER_ID, BOOK_ID, 5, "Great!");

      assertThatThrownBy(() -> review.changeStatus(null)).isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  class IsOwnedBy {

    @Test
    void shouldReturnTrueForOwner() {
      final Review review = Review.create(USER_ID, BOOK_ID, 5, "Great!");

      assertThat(review.isOwnedBy(USER_ID)).isTrue();
    }

    @Test
    void shouldReturnFalseForOtherUser() {
      final Review review = Review.create(USER_ID, BOOK_ID, 5, "Great!");
      final UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000099");

      assertThat(review.isOwnedBy(otherUserId)).isFalse();
    }
  }
}
