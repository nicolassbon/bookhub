package com.bookhub.library.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class YearlyGoalTest {

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Nested
  class Create {

    @Test
    void shouldCreateGoalWithInProgressStatus() {
      final YearlyGoal goal = YearlyGoal.create(USER_ID, 2026, 24);

      assertThat(goal.getUserId()).isEqualTo(USER_ID);
      assertThat(goal.getYear()).isEqualTo(2026);
      assertThat(goal.getTargetBooks()).isEqualTo(24);
      assertThat(goal.getCompletedBooks()).isZero();
      assertThat(goal.getStatus()).isEqualTo(GoalStatus.IN_PROGRESS);
      assertThat(goal.getCreatedAt()).isNotNull();
      assertThat(goal.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldRejectTargetOfZero() {
      assertThatThrownBy(() -> YearlyGoal.create(USER_ID, 2026, 0))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("targetBooks");
    }

    @Test
    void shouldRejectNegativeTarget() {
      assertThatThrownBy(() -> YearlyGoal.create(USER_ID, 2026, -5))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("targetBooks");
    }

    @Test
    void shouldRejectNullUserId() {
      assertThatThrownBy(() -> YearlyGoal.create(null, 2026, 12))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectTooOldYear() {
      assertThatThrownBy(() -> YearlyGoal.create(USER_ID, 1899, 12))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("year must be between");
    }
  }

  @Nested
  class UpdateTarget {

    @Test
    void shouldUpdateTargetBooks() {
      final YearlyGoal goal = YearlyGoal.create(USER_ID, 2026, 12);

      goal.updateTarget(24);

      assertThat(goal.getTargetBooks()).isEqualTo(24);
    }

    @Test
    void shouldRejectInvalidTarget() {
      final YearlyGoal goal = YearlyGoal.create(USER_ID, 2026, 12);

      assertThatThrownBy(() -> goal.updateTarget(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnToInProgressWhenTargetIncreasesAfterAchieved() {
      final YearlyGoal goal = YearlyGoal.create(USER_ID, 2026, 1);
      goal.incrementProgress();

      goal.updateTarget(2);

      assertThat(goal.getCompletedBooks()).isEqualTo(1);
      assertThat(goal.getStatus()).isEqualTo(GoalStatus.IN_PROGRESS);
    }

    @Test
    void shouldStayAchievedWhenTargetLoweredToCompletedThreshold() {
      final YearlyGoal goal = YearlyGoal.create(USER_ID, 2026, 3);
      goal.incrementProgress();
      goal.incrementProgress();

      goal.updateTarget(2);

      assertThat(goal.getCompletedBooks()).isEqualTo(2);
      assertThat(goal.getStatus()).isEqualTo(GoalStatus.ACHIEVED);
    }
  }

  @Nested
  class IncrementProgress {

    @Test
    void shouldIncrementCompletedBooks() {
      final YearlyGoal goal = YearlyGoal.create(USER_ID, 2026, 5);

      goal.incrementProgress();

      assertThat(goal.getCompletedBooks()).isEqualTo(1);
      assertThat(goal.getStatus()).isEqualTo(GoalStatus.IN_PROGRESS);
    }

    @Test
    void shouldTransitionToAchievedWhenTargetReached() {
      final YearlyGoal goal = YearlyGoal.create(USER_ID, 2026, 2);

      goal.incrementProgress();
      goal.incrementProgress();

      assertThat(goal.getCompletedBooks()).isEqualTo(2);
      assertThat(goal.getStatus()).isEqualTo(GoalStatus.ACHIEVED);
    }

    @Test
    void shouldNotIncrementBeyondTarget() {
      final YearlyGoal goal = YearlyGoal.create(USER_ID, 2026, 1);
      goal.incrementProgress();

      goal.incrementProgress();

      assertThat(goal.getCompletedBooks()).isEqualTo(1);
      assertThat(goal.getStatus()).isEqualTo(GoalStatus.ACHIEVED);
    }
  }

  @Nested
  class IsAchieved {

    @Test
    void shouldReturnFalseWhenInProgress() {
      final YearlyGoal goal = YearlyGoal.create(USER_ID, 2026, 10);

      assertThat(goal.isAchieved()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenAchieved() {
      final YearlyGoal goal = YearlyGoal.create(USER_ID, 2026, 1);
      goal.incrementProgress();

      assertThat(goal.isAchieved()).isTrue();
    }
  }
}
