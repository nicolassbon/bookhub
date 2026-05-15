package com.bookhub.library.web.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.library.domain.ReadingState;
import com.bookhub.library.domain.ReviewStatus;
import com.bookhub.library.infrastructure.persistence.JpaReviewRepository;
import com.bookhub.library.infrastructure.persistence.JpaUserBookRepository;
import com.bookhub.library.infrastructure.persistence.ReviewEntity;
import com.bookhub.library.infrastructure.persistence.UserBookEntity;
import com.bookhub.library.infrastructure.persistence.UserBookEntityTestFactory;
import com.bookhub.library.support.PostgreSqlIntegrationTest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class AdminMetricsIntegrationTest extends PostgreSqlIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JpaUserBookRepository jpaUserBookRepository;
  @Autowired private JpaReviewRepository jpaReviewRepository;

  private static final UUID ADMIN_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000999");

  @Test
  @DisplayName("Should return 403 when requesting metrics with USER role")
  void shouldReturn403WithUserRole() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/admin/metrics/library")
                .with(
                    jwt()
                        .jwt(j -> j.subject(UUID.randomUUID().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Admin receives library metrics")
  void shouldReturnLibraryMetrics() throws Exception {
    final Instant now = Instant.now();

    userBookEntity(
        UUID.fromString("00000000-0000-0000-0000-000000000100"),
        UUID.fromString("00000000-0000-0000-0000-000000000200"),
        ReadingState.WANT_TO_READ, now);
    userBookEntity(
        UUID.fromString("00000000-0000-0000-0000-000000000100"),
        UUID.fromString("00000000-0000-0000-0000-000000000201"),
        ReadingState.READING, now);
    userBookEntity(
        UUID.fromString("00000000-0000-0000-0000-000000000101"),
        UUID.fromString("00000000-0000-0000-0000-000000000202"),
        ReadingState.READ, now);

    reviewEntity(
        UUID.fromString("00000000-0000-0000-0000-000000000100"),
        UUID.fromString("00000000-0000-0000-0000-000000000200"),
        5, "Great", ReviewStatus.VISIBLE, now);
    reviewEntity(
        UUID.fromString("00000000-0000-0000-0000-000000000101"),
        UUID.fromString("00000000-0000-0000-0000-000000000202"),
        3, "Ok", ReviewStatus.FLAGGED, now);

    mockMvc
        .perform(
            get("/api/v1/admin/metrics/library")
                .with(
                    jwt()
                        .jwt(j -> j.subject(ADMIN_ID.toString()).claim("role", "ADMIN"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalUsers").value(2))
        .andExpect(jsonPath("$.totalLibraryEntries").value(3))
        .andExpect(jsonPath("$.entriesByState.wantToRead").value(1))
        .andExpect(jsonPath("$.entriesByState.reading").value(1))
        .andExpect(jsonPath("$.entriesByState.read").value(1))
        .andExpect(jsonPath("$.reviewsByStatus.approved").value(1))
        .andExpect(jsonPath("$.reviewsByStatus.pending").value(1))
        .andExpect(jsonPath("$.reviewsByStatus.rejected").value(0))
        .andExpect(jsonPath("$.totalReviews").value(2))
        .andExpect(jsonPath("$.averageRating").value(4.0));
  }

  @Test
  @DisplayName("Admin receives zero metrics when database is empty")
  void shouldReturnZeroMetricsWhenEmpty() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/admin/metrics/library")
                .with(
                    jwt()
                        .jwt(j -> j.subject(ADMIN_ID.toString()).claim("role", "ADMIN"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalUsers").value(0))
        .andExpect(jsonPath("$.totalLibraryEntries").value(0))
        .andExpect(jsonPath("$.entriesByState.wantToRead").value(0))
        .andExpect(jsonPath("$.totalReviews").value(0))
        .andExpect(jsonPath("$.averageRating").value(0.0));
  }

  private void userBookEntity(
      final UUID userId,
      final UUID bookId,
      final ReadingState state,
      final Instant now) {
    final UserBookEntity entity =
        UserBookEntityTestFactory.create(userId, bookId, state, now);
    jpaUserBookRepository.saveAndFlush(entity);
  }

  private void reviewEntity(
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
    jpaReviewRepository.saveAndFlush(entity);
  }
}
