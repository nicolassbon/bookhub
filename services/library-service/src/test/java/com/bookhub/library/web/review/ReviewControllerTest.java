package com.bookhub.library.web.review;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.library.application.GetReviewService;
import com.bookhub.library.application.ManageReviewService;
import com.bookhub.library.application.error.BookNotReadException;
import com.bookhub.library.application.error.ReviewAlreadyExistsException;
import com.bookhub.library.config.SecurityConfig;
import com.bookhub.library.domain.Review;
import com.bookhub.library.web.GlobalExceptionHandler;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReviewController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class ReviewControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ManageReviewService manageReviewService;
  @MockitoBean private GetReviewService getReviewService;
  @MockitoBean private JwtDecoder jwtDecoder;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID REVIEW_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

  @Nested
  class CreateReview {

    @Test
    void shouldReturn201WhenReviewCreated() throws Exception {
      when(manageReviewService.createReview(USER_ID, BOOK_ID, 5, "Amazing!"))
          .thenReturn(Review.create(USER_ID, BOOK_ID, 5, "Amazing!"));

      mockMvc
          .perform(
              post("/api/v1/reviews")
                  .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                       {"bookId": "00000000-0000-0000-0000-000000000002", "rating": 5, "content": "Amazing!"}
                       """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
          .andExpect(jsonPath("$.bookId").value(BOOK_ID.toString()))
          .andExpect(jsonPath("$.rating").value(5))
          .andExpect(jsonPath("$.content").value("Amazing!"))
          .andExpect(jsonPath("$.status").value("VISIBLE"));
    }

    @Test
    void shouldReturn400WhenBookNotRead() throws Exception {
      when(manageReviewService.createReview(USER_ID, BOOK_ID, 5, "Amazing!"))
          .thenThrow(new BookNotReadException("Must be read"));

      mockMvc
          .perform(
              post("/api/v1/reviews")
                  .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                       {"bookId": "00000000-0000-0000-0000-000000000002", "rating": 5, "content": "Amazing!"}
                       """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("BOOK_NOT_READ"));
    }

    @Test
    void shouldReturn400WhenRatingIsInvalid() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/reviews")
                  .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                       {"bookId": "00000000-0000-0000-0000-000000000002", "rating": 6, "content": "Amazing!"}
                       """))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn409WhenReviewAlreadyExists() throws Exception {
      when(manageReviewService.createReview(USER_ID, BOOK_ID, 5, "Amazing!"))
          .thenThrow(new ReviewAlreadyExistsException("Already exists"));

      mockMvc
          .perform(
              post("/api/v1/reviews")
                  .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"bookId": "00000000-0000-0000-0000-000000000002", "rating": 5, "content": "Amazing!"}
                      """))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.code").value("REVIEW_ALREADY_EXISTS"));
    }
  }

  @Nested
  class UpdateReview {

    @Test
    void shouldReturn200WhenReviewUpdated() throws Exception {
      final Review review = Review.create(USER_ID, BOOK_ID, 4, "Updated");
      review.setId(REVIEW_ID);
      when(manageReviewService.updateReview(USER_ID, REVIEW_ID, 4, "Updated")).thenReturn(review);

      mockMvc
          .perform(
              patch("/api/v1/reviews/{reviewId}", REVIEW_ID)
                  .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"rating": 4, "content": "Updated"}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(REVIEW_ID.toString()))
          .andExpect(jsonPath("$.rating").value(4));
    }
  }

  @Nested
  class GetReviewsForBook {

    @Test
    void shouldReturn200AndListReviews() throws Exception {
      final UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000099");
      final Review r1 = Review.create(USER_ID, BOOK_ID, 5, "Great");
      final Review r2 = Review.create(otherUserId, BOOK_ID, 3, "Okay");

      when(getReviewService.forBook(BOOK_ID)).thenReturn(List.of(r1, r2));

      mockMvc
          .perform(
              get("/api/v1/books/{bookId}/reviews", BOOK_ID)
                  .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].userId").value(USER_ID.toString()))
          .andExpect(jsonPath("$[0].rating").value(5))
          .andExpect(jsonPath("$[1].userId").value(otherUserId.toString()))
          .andExpect(jsonPath("$[1].rating").value(3));
    }

    @Test
    void shouldReturn200AndListReviewsWhenUnauthenticated() throws Exception {
      final Review r1 = Review.create(USER_ID, BOOK_ID, 5, "Great");
      when(getReviewService.forBook(BOOK_ID)).thenReturn(List.of(r1));

      mockMvc
          .perform(get("/api/v1/books/{bookId}/reviews", BOOK_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(1))
          .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    void shouldPreserveQueryParametersForUnauthenticatedReviewGet() throws Exception {
      when(getReviewService.forBook(BOOK_ID)).thenReturn(List.of());

      mockMvc
          .perform(
              get("/api/v1/books/{bookId}/reviews", BOOK_ID)
                  .param("limit", "10")
                  .param("offset", "0"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(0));
    }
  }

  @Nested
  class UnauthenticatedAccessBoundary {

    @Test
    void shouldReturn401WhenUnauthenticatedCreateReview() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/reviews")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"bookId": "00000000-0000-0000-0000-000000000002", "rating": 5, "content": "x"}
                      """))
          .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenUnauthenticatedUpdateReview() throws Exception {
      mockMvc
          .perform(
              patch("/api/v1/reviews/{reviewId}", REVIEW_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"rating": 4, "content": "x"}
                      """))
          .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenUnauthenticatedReadsReviewsCollection() throws Exception {
      mockMvc.perform(get("/api/v1/reviews")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenUnauthenticatedNonGetOnPublicReviewsPath() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/books/{bookId}/reviews", BOOK_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{}"))
          .andExpect(status().isUnauthorized());
    }
  }
}
