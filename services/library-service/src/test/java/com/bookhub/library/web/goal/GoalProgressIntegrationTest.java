package com.bookhub.library.web.goal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.library.infrastructure.client.CatalogBook;
import com.bookhub.library.infrastructure.client.CatalogServiceClient;
import com.bookhub.library.infrastructure.persistence.JpaYearlyGoalRepository;
import com.bookhub.library.support.PostgreSqlIntegrationTest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
class GoalProgressIntegrationTest extends PostgreSqlIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JpaYearlyGoalRepository jpaYearlyGoalRepository;

  @MockitoBean private CatalogServiceClient catalogServiceClient;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
  private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");

  private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor authenticatedJwt() {
    return jwt().jwt(builder -> builder.subject(USER_ID.toString()));
  }

  @Test
  @DisplayName("Completing a book increments the yearly goal progress")
  void completingBookShouldIncrementGoal() throws Exception {
    when(catalogServiceClient.findBookById(BOOK_ID))
        .thenReturn(Optional.of(new CatalogBook(BOOK_ID, "Domain-Driven Design", null, 560)));

    mockMvc
        .perform(
            put("/api/v1/goals/yearly")
                .with(authenticatedJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"year": 2026, "targetBooks": 5}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.targetBooks").value(5))
        .andExpect(jsonPath("$.completedBooks").value(0));

    final MvcResult addResult =
        mockMvc
            .perform(
                post("/api/v1/library/books")
                    .with(authenticatedJwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"bookId": "00000000-0000-0000-0000-000000000020", "initialState": "READING"}
                        """))
            .andExpect(status().isCreated())
            .andReturn();

    final String entryId =
        com.jayway.jsonpath.JsonPath.read(
            addResult.getResponse().getContentAsString(), "$.entryId");

    mockMvc
        .perform(
            patch("/api/v1/library/books/{entryId}/progress", entryId)
                .with(authenticatedJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"pagesRead": 560}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.readingState").value("READ"))
        .andExpect(jsonPath("$.completionPercentage").value(100));

    mockMvc
        .perform(get("/api/v1/goals/yearly").with(authenticatedJwt()).param("year", "2026"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.completedBooks").value(1))
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

    assertThat(jpaYearlyGoalRepository.findByUserIdAndYear(USER_ID, 2026))
        .isPresent()
        .hasValueSatisfying(g -> assertThat(g.getCompletedBooks()).isEqualTo(1));
  }

  @Test
  @DisplayName("Upsert yearly goal: updating target adjusts same goal")
  void upsertGoalUpdatesExistingTarget() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/goals/yearly")
                .with(authenticatedJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"year": 2025, "targetBooks": 10}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.targetBooks").value(10));

    mockMvc
        .perform(
            put("/api/v1/goals/yearly")
                .with(authenticatedJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"year": 2025, "targetBooks": 20}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.targetBooks").value(20));

    final long count =
        jpaYearlyGoalRepository.findAll().stream()
            .filter(g -> g.getUserId().equals(USER_ID) && g.getYear() == 2025)
            .count();
    assertThat(count).isEqualTo(1);
  }
}
