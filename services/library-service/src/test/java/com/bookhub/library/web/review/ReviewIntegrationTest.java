package com.bookhub.library.web.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.library.domain.ReviewStatus;
import com.bookhub.library.infrastructure.client.CatalogBook;
import com.bookhub.library.infrastructure.client.CatalogServiceClient;
import com.bookhub.library.infrastructure.persistence.JpaReviewRepository;
import com.bookhub.library.infrastructure.persistence.ReviewEntity;
import com.bookhub.library.support.PostgreSqlIntegrationTest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
class ReviewIntegrationTest extends PostgreSqlIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JpaReviewRepository jpaReviewRepository;

  @MockitoBean private CatalogServiceClient catalogServiceClient;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
  private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000200");

  private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor authenticatedJwt() {
    return jwt().jwt(builder -> builder.subject(USER_ID.toString()));
  }

  @Test
  @DisplayName(
      "Full Review Lifecycle: Reject unread -> Read book -> Create Review -> Update Review -> List Reviews")
  void fullReviewLifecycle() throws Exception {
    when(catalogServiceClient.findBookById(BOOK_ID))
        .thenReturn(Optional.of(new CatalogBook(BOOK_ID, "Clean Architecture", null, 400)));

    // 1. Add book to library as WANT_TO_READ
    final MvcResult addResult =
        mockMvc
            .perform(
                post("/api/v1/library/books")
                    .with(authenticatedJwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"bookId": "00000000-0000-0000-0000-000000000200", "initialState": "WANT_TO_READ"}
                        """))
            .andExpect(status().isCreated())
            .andReturn();

    final String entryId =
        com.jayway.jsonpath.JsonPath.read(
            addResult.getResponse().getContentAsString(), "$.entryId");

    // 2. Try to review -> SHOULD FAIL (400)
    mockMvc
        .perform(
            post("/api/v1/reviews")
                .with(authenticatedJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"bookId": "00000000-0000-0000-0000-000000000200", "rating": 5, "content": "Masterpiece"}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BOOK_NOT_READ"));

    // 3. Mark as READ
    mockMvc
        .perform(
            patch("/api/v1/library/books/{entryId}/state", entryId)
                .with(authenticatedJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"state": "READ"}
                    """))
        .andExpect(status().isOk());

    // 4. Try to review again -> SHOULD SUCCEED (201)
    mockMvc
        .perform(
            post("/api/v1/reviews")
                .with(authenticatedJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"bookId": "00000000-0000-0000-0000-000000000200", "rating": 4, "content": "Very good"}
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.rating").value(4))
        .andExpect(jsonPath("$.content").value("Very good"));

    final UUID reviewId = jpaReviewRepository.findByBookId(BOOK_ID).getFirst().getId();

    // 5. Update the review -> SHOULD SUCCEED (200)
    mockMvc
        .perform(
            patch("/api/v1/reviews/{reviewId}", reviewId)
                .with(authenticatedJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"rating": 5, "content": "A true masterpiece"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rating").value(5))
        .andExpect(jsonPath("$.content").value("A true masterpiece"));

    // 6. Verify directly in DB that there is only ONE review for this user+book
    assertThat(jpaReviewRepository.findByBookId(BOOK_ID)).hasSize(1);
    assertThat(jpaReviewRepository.findByBookId(BOOK_ID).getFirst().getRating()).isEqualTo(5);

    // 7. List reviews for the book
    mockMvc
        .perform(get("/api/v1/books/{bookId}/reviews", BOOK_ID).with(authenticatedJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].rating").value(5));
  }

  @Test
  @DisplayName("Database enforces one review per user and Catalog Book")
  void shouldEnforceUniqueUserAndBookReviewConstraint() {
    final UUID uniqueBookId = UUID.fromString("00000000-0000-0000-0000-000000000201");
    final Instant now = Instant.now();
    final ReviewEntity firstReview =
        reviewEntity(USER_ID, uniqueBookId, 5, "First", ReviewStatus.VISIBLE, now);

    final ReviewEntity duplicateReview =
        reviewEntity(USER_ID, uniqueBookId, 4, "Duplicate", ReviewStatus.VISIBLE, now);

    jpaReviewRepository.saveAndFlush(firstReview);

    assertThatThrownBy(() -> jpaReviewRepository.saveAndFlush(duplicateReview))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("Public listing includes only visible reviews")
  void shouldListOnlyVisibleReviewsForCatalogBook() throws Exception {
    final Instant now = Instant.now();
    final UUID listingBookId = UUID.fromString("00000000-0000-0000-0000-000000000202");
    final UUID hiddenUserId = UUID.fromString("00000000-0000-0000-0000-000000000101");

    final ReviewEntity visibleReview =
        reviewEntity(USER_ID, listingBookId, 5, "Visible review", ReviewStatus.VISIBLE, now);

    final ReviewEntity hiddenReview =
        reviewEntity(hiddenUserId, listingBookId, 1, "Hidden review", ReviewStatus.HIDDEN, now);

    jpaReviewRepository.saveAndFlush(visibleReview);
    jpaReviewRepository.saveAndFlush(hiddenReview);

    mockMvc
        .perform(get("/api/v1/books/{bookId}/reviews", listingBookId).with(authenticatedJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].content").value("Visible review"))
        .andExpect(jsonPath("$[0].status").value("VISIBLE"));
  }

  private ReviewEntity reviewEntity(
      final UUID userId,
      final UUID bookId,
      final int rating,
      final String content,
      final ReviewStatus status,
      final Instant now) {
    final ReviewEntity entity = new ReviewEntity();
    entity.setUserId(userId);
    entity.setBookId(bookId);
    entity.setRating(rating);
    entity.setContent(content);
    entity.setStatus(status);
    entity.setCreatedAt(now);
    entity.setUpdatedAt(now);
    return entity;
  }
}
