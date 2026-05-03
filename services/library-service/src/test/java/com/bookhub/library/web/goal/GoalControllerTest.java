package com.bookhub.library.web.goal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.library.application.GetYearlyGoalService;
import com.bookhub.library.application.ManageYearlyGoalService;
import com.bookhub.library.config.SecurityConfig;
import com.bookhub.library.domain.GoalStatus;
import com.bookhub.library.domain.YearlyGoal;
import com.bookhub.library.web.GlobalExceptionHandler;
import java.util.Optional;
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

@WebMvcTest(GoalController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class GoalControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ManageYearlyGoalService manageYearlyGoalService;
  @MockitoBean private GetYearlyGoalService getYearlyGoalService;
  @MockitoBean private JwtDecoder jwtDecoder;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private YearlyGoal buildGoal(final int target, final int completed, final GoalStatus status) {
    final YearlyGoal goal = YearlyGoal.create(USER_ID, 2026, target);
    for (int i = 0; i < completed; i++) goal.incrementProgress();
    return goal;
  }

  @Nested
  class UpsertGoal {

    @Test
    void shouldReturn200WhenGoalUpserted() throws Exception {
      when(manageYearlyGoalService.execute(any(), eq(2026), eq(24)))
          .thenReturn(buildGoal(24, 0, GoalStatus.IN_PROGRESS));

      mockMvc
          .perform(
              put("/api/v1/goals/yearly")
                  .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"year": 2026, "targetBooks": 24}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.year").value(2026))
          .andExpect(jsonPath("$.targetBooks").value(24))
          .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldReturn400WhenTargetIsZero() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/goals/yearly")
                  .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"year": 2026, "targetBooks": 0}
                      """))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenYearIsMissing() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/goals/yearly")
                  .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"targetBooks": 12}
                      """))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc
          .perform(
              put("/api/v1/goals/yearly")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"year": 2026, "targetBooks": 12}
                      """))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  class GetGoal {

    @Test
    void shouldReturn200WhenGoalExists() throws Exception {
      when(getYearlyGoalService.execute(any(), eq(2026)))
          .thenReturn(Optional.of(buildGoal(24, 5, GoalStatus.IN_PROGRESS)));

      mockMvc
          .perform(
              get("/api/v1/goals/yearly")
                  .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                  .param("year", "2026"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.year").value(2026))
          .andExpect(jsonPath("$.completedBooks").value(5));
    }

    @Test
    void shouldReturn404WhenGoalNotFound() throws Exception {
      when(getYearlyGoalService.execute(any(), eq(2026))).thenReturn(Optional.empty());

      mockMvc
          .perform(
              get("/api/v1/goals/yearly")
                  .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                  .param("year", "2026"))
          .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc
          .perform(get("/api/v1/goals/yearly").param("year", "2026"))
          .andExpect(status().isUnauthorized());
    }
  }
}
