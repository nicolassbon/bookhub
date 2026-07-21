package com.bookhub.library.application.admin;

import com.bookhub.library.domain.PaginatedResult;
import com.bookhub.library.domain.PaginationQuery;
import com.bookhub.library.domain.Review;
import com.bookhub.library.domain.ReviewRepository;
import com.bookhub.library.domain.ReviewStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListReviewsForModerationService {

  private final ReviewRepository reviewRepository;

  public PaginatedResult<Review> list(
      final int page, final int size, final ReviewStatus statusFilter) {
    final PaginationQuery query = new PaginationQuery(page, size);
    if (statusFilter != null) {
      return reviewRepository.findAllForModeration(query, statusFilter);
    }
    return reviewRepository.findAllForModeration(query);
  }
}
