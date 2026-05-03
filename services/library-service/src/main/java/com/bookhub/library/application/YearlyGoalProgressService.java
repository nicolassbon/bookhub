package com.bookhub.library.application;

import com.bookhub.library.domain.YearlyGoal;
import com.bookhub.library.domain.YearlyGoalRepository;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Internal service. Increments the active yearly goal when a UserBook reaches READ state. Called
 * synchronously from UpdateReadingProgressService and UpdateReadingStateService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YearlyGoalProgressService {

  private final YearlyGoalRepository yearlyGoalRepository;

  public Optional<YearlyGoal> onBookCompleted(
      final UUID userId,
      final UUID catalogBookId,
      final Instant previousFinishedAt,
      final Instant completedAt) {
    if (!shouldCountCompletion(previousFinishedAt, completedAt)) {
      final int completionYear = completedAt.atZone(ZoneOffset.UTC).getYear();
      log.info(
          "Skipping duplicate yearly goal increment for same Catalog Book and year userId={} bookId={} year={}",
          userId,
          catalogBookId,
          completionYear);
      return findGoalByYear(userId, completionYear);
    }

    final int completionYear = completedAt.atZone(ZoneOffset.UTC).getYear();
    final Optional<YearlyGoal> goal = findGoalByYear(userId, completionYear);
    goal.ifPresent(
        g -> {
          g.incrementProgress();
          yearlyGoalRepository.save(g);
          log.info(
              "Goal progress incremented userId={} year={} completed={}/{}",
              userId,
              completionYear,
              g.getCompletedBooks(),
              g.getTargetBooks());
        });
    return goal;
  }

  private Optional<YearlyGoal> findGoalByYear(final UUID userId, final int year) {
    return yearlyGoalRepository.findByUserIdAndYear(userId, year);
  }

  private boolean shouldCountCompletion(
      final Instant previousFinishedAt, final Instant completedAt) {
    if (previousFinishedAt == null) {
      return true;
    }
    final int previousYear = previousFinishedAt.atZone(ZoneOffset.UTC).getYear();
    final int completionYear = completedAt.atZone(ZoneOffset.UTC).getYear();
    return previousYear != completionYear;
  }

  /**
   * Returns the current year's goal for a user, if any. Used by notification triggers to determine
   * notification type.
   */
  public Optional<YearlyGoal> findCurrentGoal(final UUID userId) {
    return yearlyGoalRepository.findByUserIdAndYear(userId, Year.now().getValue());
  }
}
