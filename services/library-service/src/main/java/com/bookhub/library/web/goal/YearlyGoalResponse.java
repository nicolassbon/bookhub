package com.bookhub.library.web.goal;

import com.bookhub.library.domain.GoalStatus;
import com.bookhub.library.domain.YearlyGoal;
import java.util.UUID;

public record YearlyGoalResponse(
    UUID goalId,
    int year,
    int targetBooks,
    int completedBooks,
    GoalStatus status,
    Integer progressPercentage) {

  public static YearlyGoalResponse from(final YearlyGoal goal) {
    final Integer percentage =
        goal.getTargetBooks() > 0
            ? Math.min(100, goal.getCompletedBooks() * 100 / goal.getTargetBooks())
            : null;
    return new YearlyGoalResponse(
        goal.getId(),
        goal.getYear(),
        goal.getTargetBooks(),
        goal.getCompletedBooks(),
        goal.getStatus(),
        percentage);
  }
}
