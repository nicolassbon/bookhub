package com.bookhub.library.infrastructure.persistence;

import com.bookhub.library.domain.YearlyGoal;
import com.bookhub.library.domain.YearlyGoalRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class YearlyGoalRepositoryAdapter implements YearlyGoalRepository {

  private final JpaYearlyGoalRepository jpa;
  private final YearlyGoalEntityMapper mapper;

  @Override
  public Optional<YearlyGoal> findByUserIdAndYear(final UUID userId, final int year) {
    return jpa.findByUserIdAndYear(userId, year).map(mapper::toDomain);
  }

  @Override
  public YearlyGoal save(final YearlyGoal goal) {
    final YearlyGoalEntity entity = mapper.toEntity(goal);
    final YearlyGoalEntity saved = jpa.save(entity);
    return mapper.toDomain(saved);
  }
}
