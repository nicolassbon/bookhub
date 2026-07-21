package com.bookhub.library.web.admin;

import com.bookhub.library.application.admin.ListReviewsForModerationService;
import com.bookhub.library.application.admin.ModerateReviewService;
import com.bookhub.library.domain.PaginatedResult;
import com.bookhub.library.domain.Review;
import com.bookhub.library.domain.ReviewStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminReviewController {

  private final ListReviewsForModerationService listReviewsService;
  private final ModerateReviewService moderateReviewService;

  public AdminReviewController(
      final ListReviewsForModerationService listReviewsService,
      final ModerateReviewService moderateReviewService) {
    this.listReviewsService = listReviewsService;
    this.moderateReviewService = moderateReviewService;
  }

  @GetMapping("/reviews")
  public ResponseEntity<PagedAdminReviewResponse> listReviews(
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "20") final int size,
      @RequestParam(required = false) final String status) {

    final ReviewStatus statusFilter = status != null ? parseReviewStatus(status) : null;
    final PaginatedResult<Review> result = listReviewsService.list(page, size, statusFilter);

    final List<AdminReviewResponse> items =
        result.items().stream().map(AdminReviewResponse::from).toList();

    return ResponseEntity.ok(
        new PagedAdminReviewResponse(
            items, result.page(), result.size(), result.totalElements(), result.totalPages()));
  }

  @PatchMapping("/reviews/{reviewId}/status")
  public ResponseEntity<AdminReviewResponse> moderateReview(
      @PathVariable final UUID reviewId, @Valid @RequestBody final ModerateReviewRequest request) {

    final ReviewStatus newStatus = parseModerationStatus(request.status());
    final Review updated = moderateReviewService.moderate(reviewId, newStatus);
    return ResponseEntity.ok(AdminReviewResponse.from(updated));
  }

  private ReviewStatus parseReviewStatus(final String status) {
    try {
      return ReviewStatus.valueOf(status.toUpperCase());
    } catch (final IllegalArgumentException e) {
      throw new InvalidReviewStatusException(status);
    }
  }

  private ReviewStatus parseModerationStatus(final String status) {
    return switch (status.toUpperCase()) {
      case "APPROVED" -> ReviewStatus.VISIBLE;
      case "REJECTED" -> ReviewStatus.HIDDEN;
      case "FLAGGED" -> ReviewStatus.FLAGGED;
      default -> throw new InvalidReviewStatusException(status);
    };
  }
}
