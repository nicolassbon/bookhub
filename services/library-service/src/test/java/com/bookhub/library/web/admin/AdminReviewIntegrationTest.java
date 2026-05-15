package com.bookhub.library.web.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.library.domain.ReviewStatus;
import com.bookhub.library.infrastructure.persistence.JpaReviewRepository;
import com.bookhub.library.infrastructure.persistence.ReviewEntity;
import com.bookhub.library.support.PostgreSqlIntegrationTest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class AdminReviewIntegrationTest extends PostgreSqlIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JpaReviewRepository jpaReviewRepository;

  private static final UUID USER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000100");
  private static final UUID BOOK_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000200");
  private static final UUID ADMIN_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000999");

  @Test
  @DisplayName("Should return 403 when listing reviews with USER role")
  void shouldReturn403WhenListingReviewsWithUserRole() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/admin/reviews")
                .with(jwt().jwt(j -> j.subject(USER_ID.toString())).authorities(
                    new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Should return 401 when listing reviews without authentication")
  void shouldReturn401WhenListingReviewsWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/admin/reviews")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Admin lists reviews with pagination")
  void shouldListReviewsWithPagination() throws Exception {
    final Instant now = Instant.now();
    jpaReviewRepository.saveAndFlush(
        reviewEntity(USER_ID, BOOK_ID, 5, "Great", ReviewStatus.VISIBLE, now));
    jpaReviewRepository.saveAndFlush(
        reviewEntity(
            UUID.fromString("00000000-0000-0000-0000-000000000101"),
            BOOK_ID, 1, "Spam", ReviewStatus.FLAGGED, now));

    mockMvc
        .perform(
            get("/api/v1/admin/reviews")
                .with(
                    jwt()
                        .jwt(j -> j.subject(ADMIN_ID.toString()).claim("role", "ADMIN"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(2))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20));
  }

  @Test
  @DisplayName("Admin filters reviews by status")
  void shouldFilterReviewsByStatus() throws Exception {
    final Instant now = Instant.now();
    jpaReviewRepository.saveAndFlush(
        reviewEntity(USER_ID, BOOK_ID, 5, "Great", ReviewStatus.VISIBLE, now));
    jpaReviewRepository.saveAndFlush(
        reviewEntity(
            UUID.fromString("00000000-0000-0000-0000-000000000101"),
            BOOK_ID, 1, "Spam", ReviewStatus.FLAGGED, now));

    mockMvc
        .perform(
            get("/api/v1/admin/reviews")
                .param("status", "FLAGGED")
                .with(
                    jwt()
                        .jwt(j -> j.subject(ADMIN_ID.toString()).claim("role", "ADMIN"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].status").value("FLAGGED"));
  }

  @Test
  @DisplayName("Admin approves a review")
  void shouldApproveReview() throws Exception {
    final Instant now = Instant.now();
    final ReviewEntity review =
        reviewEntity(USER_ID, BOOK_ID, 4, "Decent", ReviewStatus.FLAGGED, now);
    final ReviewEntity saved = jpaReviewRepository.saveAndFlush(review);

    mockMvc
        .perform(
            patch("/api/v1/admin/reviews/{reviewId}/status", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {"status": "APPROVED"}
                         """)
                .with(
                    jwt()
                        .jwt(j -> j.subject(ADMIN_ID.toString()).claim("role", "ADMIN"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reviewId").value(saved.getId().toString()))
        .andExpect(jsonPath("$.status").value("VISIBLE"));
  }

  @Test
  @DisplayName("Admin rejects a review")
  void shouldRejectReview() throws Exception {
    final Instant now = Instant.now();
    final ReviewEntity review =
        reviewEntity(USER_ID, BOOK_ID, 1, "Toxic", ReviewStatus.FLAGGED, now);
    final ReviewEntity saved = jpaReviewRepository.saveAndFlush(review);

    mockMvc
        .perform(
            patch("/api/v1/admin/reviews/{reviewId}/status", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {"status": "REJECTED"}
                         """)
                .with(
                    jwt()
                        .jwt(j -> j.subject(ADMIN_ID.toString()).claim("role", "ADMIN"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("HIDDEN"));
  }

  @Test
  @DisplayName("Should return 404 when moderating non-existent review")
  void shouldReturn404ForNonExistentReview() throws Exception {
    final UUID nonExistentId = UUID.fromString("00000000-0000-0000-0000-999999999999");

    mockMvc
        .perform(
            patch("/api/v1/admin/reviews/{reviewId}/status", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {"status": "APPROVED"}
                         """)
                .with(
                    jwt()
                        .jwt(j -> j.subject(ADMIN_ID.toString()).claim("role", "ADMIN"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("REVIEW_NOT_FOUND"));
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
