package com.bookhub.library.web.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.library.application.admin.ListReviewsForModerationService;
import com.bookhub.library.application.admin.ModerateReviewService;
import com.bookhub.library.application.error.ReviewNotFoundException;
import com.bookhub.library.domain.PaginatedResult;
import com.bookhub.library.domain.Review;
import com.bookhub.library.domain.ReviewStatus;
import com.bookhub.library.web.GlobalExceptionHandler;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AdminReviewControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ListReviewsForModerationService listReviewsService;
  @MockitoBean private ModerateReviewService moderateReviewService;

  private static final UUID REVIEW_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

  @Nested
  class ListReviewsForModeration {

    @Test
    void shouldReturnPaginatedReviewList() throws Exception {
      final Review review =
          Review.rehydrateBuilder()
              .id(REVIEW_ID)
              .userId(USER_ID)
              .bookId(BOOK_ID)
              .rating(4)
              .content("Good book")
              .status(ReviewStatus.FLAGGED)
              .createdAt(java.time.Instant.parse("2026-05-09T10:00:00Z"))
              .updatedAt(java.time.Instant.parse("2026-05-09T10:00:00Z"))
              .build();

      when(listReviewsService.list(eq(0), eq(20), eq(null)))
          .thenReturn(new PaginatedResult<>(List.of(review), 0, 20, 1, 1));

      mockMvc
          .perform(get("/api/v1/admin/reviews").param("page", "0").param("size", "20"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.page").value(0))
          .andExpect(jsonPath("$.size").value(20))
          .andExpect(jsonPath("$.totalElements").value(1))
          .andExpect(jsonPath("$.totalPages").value(1))
          .andExpect(jsonPath("$.items[0].reviewId").value(REVIEW_ID.toString()))
          .andExpect(jsonPath("$.items[0].userId").value(USER_ID.toString()))
          .andExpect(jsonPath("$.items[0].bookId").value(BOOK_ID.toString()))
          .andExpect(jsonPath("$.items[0].rating").value(4))
          .andExpect(jsonPath("$.items[0].content").value("Good book"))
          .andExpect(jsonPath("$.items[0].status").value("FLAGGED"))
          .andExpect(jsonPath("$.items[0].createdAt").value("2026-05-09T10:00:00Z"));
    }

    @Test
    void shouldFilterReviewsByStatus() throws Exception {
      final Review hiddenReview =
          Review.rehydrateBuilder()
              .id(REVIEW_ID)
              .userId(USER_ID)
              .bookId(BOOK_ID)
              .rating(1)
              .content("Spam")
              .status(ReviewStatus.HIDDEN)
              .createdAt(java.time.Instant.parse("2026-05-09T10:00:00Z"))
              .updatedAt(java.time.Instant.parse("2026-05-09T10:00:00Z"))
              .build();

      when(listReviewsService.list(eq(0), eq(20), eq(ReviewStatus.HIDDEN)))
          .thenReturn(new PaginatedResult<>(List.of(hiddenReview), 0, 20, 1, 1));

      mockMvc
          .perform(
              get("/api/v1/admin/reviews")
                  .param("page", "0")
                  .param("size", "20")
                  .param("status", "HIDDEN"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.items[0].status").value("HIDDEN"))
          .andExpect(jsonPath("$.items[0].content").value("Spam"));
    }

    @Test
    void shouldReturn400WhenStatusParamIsInvalid() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/admin/reviews")
                  .param("page", "0")
                  .param("size", "20")
                  .param("status", "INVALID"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
  }

  @Nested
  class ModerateReview {

    @Test
    void shouldApproveReview() throws Exception {
      final Review approved =
          Review.rehydrateBuilder()
              .id(REVIEW_ID)
              .userId(USER_ID)
              .bookId(BOOK_ID)
              .rating(4)
              .content("Good book")
              .status(ReviewStatus.VISIBLE)
              .createdAt(java.time.Instant.parse("2026-05-09T10:00:00Z"))
              .updatedAt(java.time.Instant.parse("2026-05-09T10:00:00Z"))
              .build();

      when(moderateReviewService.moderate(eq(REVIEW_ID), eq(ReviewStatus.VISIBLE)))
          .thenReturn(approved);

      mockMvc
          .perform(
              patch("/api/v1/admin/reviews/{reviewId}/status", REVIEW_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                           {"status": "APPROVED"}
                           """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.reviewId").value(REVIEW_ID.toString()))
          .andExpect(jsonPath("$.status").value("VISIBLE"));
    }

    @Test
    void shouldRejectReview() throws Exception {
      final Review rejected =
          Review.rehydrateBuilder()
              .id(REVIEW_ID)
              .userId(USER_ID)
              .bookId(BOOK_ID)
              .rating(1)
              .content("Spam")
              .status(ReviewStatus.HIDDEN)
              .createdAt(java.time.Instant.parse("2026-05-09T10:00:00Z"))
              .updatedAt(java.time.Instant.parse("2026-05-09T10:00:00Z"))
              .build();

      when(moderateReviewService.moderate(eq(REVIEW_ID), eq(ReviewStatus.HIDDEN)))
          .thenReturn(rejected);

      mockMvc
          .perform(
              patch("/api/v1/admin/reviews/{reviewId}/status", REVIEW_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                           {"status": "REJECTED"}
                           """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("HIDDEN"));
    }

    @Test
    void shouldFlagReview() throws Exception {
      final Review flagged =
          Review.rehydrateBuilder()
              .id(REVIEW_ID)
              .userId(USER_ID)
              .bookId(BOOK_ID)
              .rating(3)
              .content("Suspicious")
              .status(ReviewStatus.FLAGGED)
              .createdAt(java.time.Instant.parse("2026-05-09T10:00:00Z"))
              .updatedAt(java.time.Instant.parse("2026-05-09T10:00:00Z"))
              .build();

      when(moderateReviewService.moderate(eq(REVIEW_ID), eq(ReviewStatus.FLAGGED)))
          .thenReturn(flagged);

      mockMvc
          .perform(
              patch("/api/v1/admin/reviews/{reviewId}/status", REVIEW_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                           {"status": "FLAGGED"}
                           """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("FLAGGED"));
    }

    @Test
    void shouldReturnNotFoundWhenReviewDoesNotExist() throws Exception {
      when(moderateReviewService.moderate(eq(REVIEW_ID), any()))
          .thenThrow(new ReviewNotFoundException(REVIEW_ID.toString()));

      mockMvc
          .perform(
              patch("/api/v1/admin/reviews/{reviewId}/status", REVIEW_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                           {"status": "APPROVED"}
                           """))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("REVIEW_NOT_FOUND"));
    }

    @Test
    void shouldReturn400WhenStatusIsInvalid() throws Exception {
      mockMvc
          .perform(
              patch("/api/v1/admin/reviews/{reviewId}/status", REVIEW_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                           {"status": "INVALID"}
                           """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturn400WhenStatusFieldIsMissing() throws Exception {
      mockMvc
          .perform(
              patch("/api/v1/admin/reviews/{reviewId}/status", REVIEW_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
  }

  @Nested
  class Authorization {

    // Authorization tests via addFilters = false bypass security filters.
    // Security integration is tested separately via integration tests.
    // Controller-level validation tests live in the subclasses above.
  }
}
