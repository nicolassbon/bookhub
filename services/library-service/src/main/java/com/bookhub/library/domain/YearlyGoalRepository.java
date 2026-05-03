package com.bookhub.library.domain;

import java.util.Optional;
import java.util.UUID;

public interface YearlyGoalRepository {

  Optional<YearlyGoal> findByUserIdAndYear(UUID userId, int year);

  YearlyGoal save(YearlyGoal goal);
}
