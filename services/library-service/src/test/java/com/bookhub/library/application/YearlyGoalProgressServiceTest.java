package com.bookhub.library.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhub.library.domain.YearlyGoal;
import com.bookhub.library.domain.YearlyGoalRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YearlyGoalProgressServiceTest {

  @Mock private YearlyGoalRepository yearlyGoalRepository;

  @InjectMocks private YearlyGoalProgressService service;

  @Test
  void shouldNotCountSameCatalogBookTwiceInSameCalendarYear() {
    final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    final UUID bookId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    final Instant previousFinishedAt = Instant.parse("2026-02-10T10:00:00Z");
    final Instant completedAt = Instant.parse("2026-09-10T10:00:00Z");
    final YearlyGoal goal = YearlyGoal.create(userId, 2026, 12);
    when(yearlyGoalRepository.findByUserIdAndYear(userId, 2026)).thenReturn(Optional.of(goal));

    service.onBookCompleted(userId, bookId, previousFinishedAt, completedAt);

    verify(yearlyGoalRepository, never()).save(any());
  }

  @Test
  void shouldCountSameCatalogBookAgainInDifferentCalendarYear() {
    final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    final UUID bookId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    final Instant previousFinishedAt = Instant.parse("2025-12-30T10:00:00Z");
    final Instant completedAt = Instant.parse("2026-01-02T10:00:00Z");
    final YearlyGoal goal = YearlyGoal.create(userId, 2026, 12);
    when(yearlyGoalRepository.findByUserIdAndYear(userId, 2026)).thenReturn(Optional.of(goal));

    service.onBookCompleted(userId, bookId, previousFinishedAt, completedAt);

    verify(yearlyGoalRepository).save(goal);
  }
}
