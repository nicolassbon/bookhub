package com.bookhub.library.application.admin;

import com.bookhub.library.application.error.ReviewNotFoundException;
import com.bookhub.library.domain.Review;
import com.bookhub.library.domain.ReviewRepository;
import com.bookhub.library.domain.ReviewStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModerateReviewService {

  private final ReviewRepository reviewRepository;

  @Transactional
  public Review moderate(final UUID reviewId, final ReviewStatus newStatus) {
    final Review review =
        reviewRepository
            .findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException(reviewId.toString()));
    review.changeStatus(newStatus);
    return reviewRepository.save(review);
  }
}
