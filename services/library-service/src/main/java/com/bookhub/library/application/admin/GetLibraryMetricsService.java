package com.bookhub.library.application.admin;

import com.bookhub.library.domain.ReadingState;
import com.bookhub.library.domain.ReviewStatus;
import com.bookhub.library.infrastructure.persistence.JpaReviewRepository;
import com.bookhub.library.infrastructure.persistence.JpaUserBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetLibraryMetricsService {

  private final JpaUserBookRepository jpaUserBookRepository;
  private final JpaReviewRepository jpaReviewRepository;

  public LibraryMetricsResult compute() {
    final long totalLibraryEntries = jpaUserBookRepository.count();
    final long totalUsers = jpaUserBookRepository.countDistinctUserIds();
    final long wantToRead = jpaUserBookRepository.countByState(ReadingState.WANT_TO_READ);
    final long reading = jpaUserBookRepository.countByState(ReadingState.READING);
    final long read = jpaUserBookRepository.countByState(ReadingState.READ);

    final long totalReviews = jpaReviewRepository.count();
    final long pending = jpaReviewRepository.countByStatus(ReviewStatus.FLAGGED);
    final long approved = jpaReviewRepository.countByStatus(ReviewStatus.VISIBLE);
    final long rejected = jpaReviewRepository.countByStatus(ReviewStatus.HIDDEN);
    final double averageRating = jpaReviewRepository.computeAverageRating();

    return new LibraryMetricsResult(
        totalUsers, totalLibraryEntries,
        wantToRead, reading, read,
        pending, approved, rejected,
        totalReviews, averageRating);
  }

  public record LibraryMetricsResult(
      long totalUsers,
      long totalLibraryEntries,
      long wantToRead,
      long reading,
      long read,
      long pendingReviews,
      long approvedReviews,
      long rejectedReviews,
      long totalReviews,
      double averageRating) {}
}
