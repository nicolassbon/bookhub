package com.bookhub.library.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaYearlyGoalRepository extends JpaRepository<YearlyGoalEntity, UUID> {

  Optional<YearlyGoalEntity> findByUserIdAndYear(UUID userId, int year);
}
