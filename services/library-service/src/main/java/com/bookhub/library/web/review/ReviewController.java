package com.bookhub.library.web.review;

import com.bookhub.library.application.GetReviewService;
import com.bookhub.library.application.ManageReviewService;
import com.bookhub.library.domain.Review;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

  private final ManageReviewService manageReviewService;
  private final GetReviewService getReviewService;

  @PostMapping("/reviews")
  public ResponseEntity<ReviewResponse> createReview(
      @AuthenticationPrincipal final Jwt jwt,
      @Valid @RequestBody final CreateReviewRequest request) {
    final UUID userId = UUID.fromString(jwt.getSubject());
    final Review review =
        manageReviewService.createReview(
            userId, request.bookId(), request.rating(), request.content());
    return ResponseEntity.status(HttpStatus.CREATED).body(ReviewResponse.from(review));
  }

  @PatchMapping("/reviews/{reviewId}")
  public ResponseEntity<ReviewResponse> updateReview(
      @PathVariable final UUID reviewId,
      @AuthenticationPrincipal final Jwt jwt,
      @Valid @RequestBody final UpdateReviewRequest request) {
    final UUID userId = UUID.fromString(jwt.getSubject());
    final Review review =
        manageReviewService.updateReview(userId, reviewId, request.rating(), request.content());
    return ResponseEntity.ok(ReviewResponse.from(review));
  }

  @GetMapping("/books/{bookId}/reviews")
  public ResponseEntity<List<ReviewResponse>> getReviews(@PathVariable final UUID bookId) {
    final List<ReviewResponse> responses =
        getReviewService.forBook(bookId).stream().map(ReviewResponse::from).toList();
    return ResponseEntity.ok(responses);
  }
}
