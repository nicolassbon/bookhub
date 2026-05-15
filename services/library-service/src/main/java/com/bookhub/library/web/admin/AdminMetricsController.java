package com.bookhub.library.web.admin;

import com.bookhub.library.application.admin.GetLibraryMetricsService;
import com.bookhub.library.application.admin.GetLibraryMetricsService.LibraryMetricsResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/metrics")
public class AdminMetricsController {

  private final GetLibraryMetricsService getLibraryMetricsService;

  public AdminMetricsController(final GetLibraryMetricsService getLibraryMetricsService) {
    this.getLibraryMetricsService = getLibraryMetricsService;
  }

  @GetMapping("/library")
  public ResponseEntity<LibraryMetricsResponse> getLibraryMetrics() {
    final LibraryMetricsResult result = getLibraryMetricsService.compute();
    return ResponseEntity.ok(
        new LibraryMetricsResponse(
            result.totalUsers(),
            result.totalLibraryEntries(),
            new LibraryMetricsResponse.StateCounts(
                result.wantToRead(), result.reading(), result.read()),
            new LibraryMetricsResponse.ReviewCounts(
                result.pendingReviews(), result.approvedReviews(), result.rejectedReviews()),
            result.totalReviews(),
            result.averageRating()));
  }
}
