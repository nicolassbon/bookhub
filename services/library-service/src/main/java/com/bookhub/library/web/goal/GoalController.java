package com.bookhub.library.web.goal;

import com.bookhub.library.application.GetYearlyGoalService;
import com.bookhub.library.application.ManageYearlyGoalService;
import com.bookhub.library.application.error.YearlyGoalNotFoundException;
import com.bookhub.library.domain.YearlyGoal;
import jakarta.validation.Valid;
import java.time.Year;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalController {

  private final ManageYearlyGoalService manageYearlyGoalService;
  private final GetYearlyGoalService getYearlyGoalService;

  @PutMapping("/yearly")
  public ResponseEntity<YearlyGoalResponse> upsertYearlyGoal(
      @AuthenticationPrincipal final Jwt jwt,
      @Valid @RequestBody final UpsertYearlyGoalRequest request) {
    final UUID userId = UUID.fromString(jwt.getSubject());
    final YearlyGoal goal =
        manageYearlyGoalService.execute(userId, request.year(), request.targetBooks());
    return ResponseEntity.ok(YearlyGoalResponse.from(goal));
  }

  @GetMapping("/yearly")
  public ResponseEntity<YearlyGoalResponse> getYearlyGoal(
      @AuthenticationPrincipal final Jwt jwt, @RequestParam(required = false) final Integer year) {
    final UUID userId = UUID.fromString(jwt.getSubject());
    final int resolvedYear = year != null ? year : Year.now().getValue();
    return getYearlyGoalService
        .execute(userId, resolvedYear)
        .map(YearlyGoalResponse::from)
        .map(ResponseEntity::ok)
        .orElseThrow(
            () -> new YearlyGoalNotFoundException("No yearly goal found for year " + resolvedYear));
  }
}
