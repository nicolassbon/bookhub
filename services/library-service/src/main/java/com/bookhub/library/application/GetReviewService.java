package com.bookhub.library.application;

import com.bookhub.library.domain.Review;
import com.bookhub.library.domain.ReviewRepository;
import com.bookhub.library.domain.ReviewStatus;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetReviewService {

  private final ReviewRepository reviewRepository;

  public List<Review> forBook(final UUID bookId) {
    return reviewRepository.findByBookIdAndStatus(bookId, ReviewStatus.VISIBLE);
  }
}
