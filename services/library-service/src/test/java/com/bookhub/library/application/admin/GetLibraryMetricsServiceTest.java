package com.bookhub.library.application.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.bookhub.library.application.admin.GetLibraryMetricsService.LibraryMetricsResult;
import com.bookhub.library.domain.ReadingState;
import com.bookhub.library.domain.ReviewStatus;
import com.bookhub.library.infrastructure.persistence.JpaReviewRepository;
import com.bookhub.library.infrastructure.persistence.JpaUserBookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetLibraryMetricsServiceTest {

  @Mock private JpaUserBookRepository jpaUserBookRepository;
  @Mock private JpaReviewRepository jpaReviewRepository;

  @InjectMocks private GetLibraryMetricsService getLibraryMetricsService;

  @Test
  void shouldComputeLibraryMetrics() {
    when(jpaUserBookRepository.count()).thenReturn(150L);
    when(jpaUserBookRepository.countDistinctUserIds()).thenReturn(42L);
    when(jpaUserBookRepository.countByState(ReadingState.WANT_TO_READ)).thenReturn(30L);
    when(jpaUserBookRepository.countByState(ReadingState.READING)).thenReturn(50L);
    when(jpaUserBookRepository.countByState(ReadingState.READ)).thenReturn(70L);
    when(jpaReviewRepository.count()).thenReturn(120L);
    when(jpaReviewRepository.countByStatus(ReviewStatus.FLAGGED)).thenReturn(15L);
    when(jpaReviewRepository.countByStatus(ReviewStatus.VISIBLE)).thenReturn(120L);
    when(jpaReviewRepository.countByStatus(ReviewStatus.HIDDEN)).thenReturn(10L);
    when(jpaReviewRepository.computeAverageRating()).thenReturn(4.2);

    final LibraryMetricsResult metrics = getLibraryMetricsService.compute();

    assertThat(metrics.totalUsers()).isEqualTo(42);
    assertThat(metrics.totalLibraryEntries()).isEqualTo(150);
    assertThat(metrics.wantToRead()).isEqualTo(30);
    assertThat(metrics.reading()).isEqualTo(50);
    assertThat(metrics.read()).isEqualTo(70);
    assertThat(metrics.pendingReviews()).isEqualTo(15);
    assertThat(metrics.approvedReviews()).isEqualTo(120);
    assertThat(metrics.rejectedReviews()).isEqualTo(10);
    assertThat(metrics.totalReviews()).isEqualTo(120);
    assertThat(metrics.averageRating()).isEqualTo(4.2);
  }

  @Test
  void shouldReturnZeroMetricsWhenDatabaseIsEmpty() {
    when(jpaUserBookRepository.count()).thenReturn(0L);
    when(jpaUserBookRepository.countDistinctUserIds()).thenReturn(0L);
    when(jpaUserBookRepository.countByState(ReadingState.WANT_TO_READ)).thenReturn(0L);
    when(jpaUserBookRepository.countByState(ReadingState.READING)).thenReturn(0L);
    when(jpaUserBookRepository.countByState(ReadingState.READ)).thenReturn(0L);
    when(jpaReviewRepository.count()).thenReturn(0L);
    when(jpaReviewRepository.countByStatus(ReviewStatus.FLAGGED)).thenReturn(0L);
    when(jpaReviewRepository.countByStatus(ReviewStatus.VISIBLE)).thenReturn(0L);
    when(jpaReviewRepository.countByStatus(ReviewStatus.HIDDEN)).thenReturn(0L);
    when(jpaReviewRepository.computeAverageRating()).thenReturn(0.0);

    final LibraryMetricsResult metrics = getLibraryMetricsService.compute();

    assertThat(metrics.totalUsers()).isEqualTo(0);
    assertThat(metrics.totalLibraryEntries()).isEqualTo(0);
    assertThat(metrics.wantToRead()).isEqualTo(0);
    assertThat(metrics.averageRating()).isEqualTo(0.0);
    assertThat(metrics.totalReviews()).isEqualTo(0);
  }
}
