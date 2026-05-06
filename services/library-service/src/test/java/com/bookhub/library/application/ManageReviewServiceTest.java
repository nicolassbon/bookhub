package com.bookhub.library.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhub.library.application.error.BookNotReadException;
import com.bookhub.library.application.error.LibraryEntryNotFoundException;
import com.bookhub.library.application.error.LibraryEntryOwnershipException;
import com.bookhub.library.application.error.ReviewAlreadyExistsException;
import com.bookhub.library.domain.BookSnapshot;
import com.bookhub.library.domain.ReadingState;
import com.bookhub.library.domain.Review;
import com.bookhub.library.domain.ReviewRepository;
import com.bookhub.library.domain.UserBook;
import com.bookhub.library.domain.UserBookRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManageReviewServiceTest {

  @Mock private ReviewRepository reviewRepository;
  @Mock private UserBookRepository userBookRepository;

  @InjectMocks private ManageReviewService manageReviewService;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Nested
  class CreateReview {

    @Test
    void shouldCreateNewReviewWhenBookIsRead() {
      final UserBook userBook =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.WANT_TO_READ, new BookSnapshot("Title", null, 100));
      userBook.updateState(ReadingState.READ);
      when(userBookRepository.findByUserIdAndBookId(USER_ID, BOOK_ID))
          .thenReturn(Optional.of(userBook));
      when(reviewRepository.findByUserIdAndBookId(USER_ID, BOOK_ID)).thenReturn(Optional.empty());
      when(reviewRepository.save(any(Review.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      final Review review = manageReviewService.createReview(USER_ID, BOOK_ID, 5, "Amazing book!");

      assertThat(review.getRating()).isEqualTo(5);
      assertThat(review.getContent()).isEqualTo("Amazing book!");

      final ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
      verify(reviewRepository).save(reviewCaptor.capture());
      assertThat(reviewCaptor.getValue().getRating()).isEqualTo(5);
    }

    @Test
    void shouldThrowWhenReviewAlreadyExistsForCatalogBook() {
      final UserBook userBook =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.WANT_TO_READ, new BookSnapshot("Title", null, 100));
      userBook.updateState(ReadingState.READ);
      when(userBookRepository.findByUserIdAndBookId(USER_ID, BOOK_ID))
          .thenReturn(Optional.of(userBook));

      final Review existingReview = Review.create(USER_ID, BOOK_ID, 3, "It was okay.");
      when(reviewRepository.findByUserIdAndBookId(USER_ID, BOOK_ID))
          .thenReturn(Optional.of(existingReview));

      assertThatThrownBy(() -> manageReviewService.createReview(USER_ID, BOOK_ID, 4, "Duplicate"))
          .isInstanceOf(ReviewAlreadyExistsException.class);
    }

    @Test
    void shouldThrowBookNotReadExceptionWhenBookIsNotInLibrary() {
      when(userBookRepository.findByUserIdAndBookId(USER_ID, BOOK_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> manageReviewService.createReview(USER_ID, BOOK_ID, 5, "Nice!"))
          .isInstanceOf(BookNotReadException.class)
          .hasMessageContaining("must be read");
    }

    @Test
    void shouldThrowBookNotReadExceptionWhenBookIsNotInReadState() {
      final UserBook userBook =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.WANT_TO_READ, new BookSnapshot("Title", null, 100));
      userBook.updateState(ReadingState.READING);
      when(userBookRepository.findByUserIdAndBookId(USER_ID, BOOK_ID))
          .thenReturn(Optional.of(userBook));

      assertThatThrownBy(() -> manageReviewService.createReview(USER_ID, BOOK_ID, 5, "Nice!"))
          .isInstanceOf(BookNotReadException.class)
          .hasMessageContaining("must be read");
    }
  }

  @Nested
  class UpdateReview {

    @Test
    void shouldUpdateOwnedReview() {
      final UUID reviewId = UUID.fromString("00000000-0000-0000-0000-000000000003");
      final Review existingReview = Review.create(USER_ID, BOOK_ID, 3, "Old");
      existingReview.setId(reviewId);
      when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
      when(reviewRepository.save(any(Review.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      final Review updated = manageReviewService.updateReview(USER_ID, reviewId, 5, "New");

      assertThat(updated.getRating()).isEqualTo(5);
      assertThat(updated.getContent()).isEqualTo("New");
      verify(reviewRepository).save(existingReview);
    }

    @Test
    void shouldThrowWhenReviewNotFound() {
      final UUID reviewId = UUID.fromString("00000000-0000-0000-0000-000000000003");
      when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> manageReviewService.updateReview(USER_ID, reviewId, 5, "New"))
          .isInstanceOf(LibraryEntryNotFoundException.class);
    }

    @Test
    void shouldThrowWhenReviewOwnedByAnotherUser() {
      final UUID reviewId = UUID.fromString("00000000-0000-0000-0000-000000000003");
      final UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000099");
      final Review existingReview = Review.create(otherUserId, BOOK_ID, 3, "Old");
      existingReview.setId(reviewId);
      when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));

      assertThatThrownBy(() -> manageReviewService.updateReview(USER_ID, reviewId, 5, "New"))
          .isInstanceOf(LibraryEntryOwnershipException.class);
    }
  }
}
