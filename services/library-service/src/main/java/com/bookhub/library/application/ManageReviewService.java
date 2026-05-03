package com.bookhub.library.application;

import com.bookhub.library.application.error.BookNotReadException;
import com.bookhub.library.application.error.LibraryEntryNotFoundException;
import com.bookhub.library.application.error.LibraryEntryOwnershipException;
import com.bookhub.library.application.error.ReviewAlreadyExistsException;
import com.bookhub.library.domain.ReadingState;
import com.bookhub.library.domain.Review;
import com.bookhub.library.domain.ReviewRepository;
import com.bookhub.library.domain.UserBook;
import com.bookhub.library.domain.UserBookRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageReviewService {

  private final ReviewRepository reviewRepository;
  private final UserBookRepository userBookRepository;

  @Transactional
  public Review createReview(
      final UUID userId, final UUID bookId, final int rating, final String content) {
    verifyBookIsRead(userId, bookId);

    if (reviewRepository.findByUserIdAndBookId(userId, bookId).isPresent()) {
      throw new ReviewAlreadyExistsException(
          "Review already exists for user " + userId + " and Catalog Book " + bookId);
    }

    log.info("Creating review for userId={} bookId={} rating={}", userId, bookId, rating);
    final Review review = Review.create(userId, bookId, rating, content);
    return reviewRepository.save(review);
  }

  @Transactional
  public Review updateReview(
      final UUID userId, final UUID reviewId, final int rating, final String content) {
    final Review review =
        reviewRepository
            .findById(reviewId)
            .orElseThrow(
                () -> new LibraryEntryNotFoundException("Review " + reviewId + " not found"));

    if (!review.isOwnedBy(userId)) {
      throw new LibraryEntryOwnershipException(
          "User " + userId + " does not own review " + reviewId);
    }

    review.editContent(rating, content);
    return reviewRepository.save(review);
  }

  private void verifyBookIsRead(final UUID userId, final UUID bookId) {
    final boolean isRead =
        userBookRepository
            .findByUserIdAndBookId(userId, bookId)
            .map(UserBook::getState)
            .filter(state -> state == ReadingState.READ)
            .isPresent();

    if (!isRead) {
      throw new BookNotReadException(
          "Book " + bookId + " must be read by user " + userId + " before reviewing");
    }
  }
}
