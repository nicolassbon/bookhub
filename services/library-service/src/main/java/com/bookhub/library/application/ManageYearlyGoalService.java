package com.bookhub.library.application;

import com.bookhub.library.domain.YearlyGoal;
import com.bookhub.library.domain.YearlyGoalRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageYearlyGoalService {

  private final YearlyGoalRepository yearlyGoalRepository;

  @Transactional
  public YearlyGoal execute(final UUID userId, final int year, final int targetBooks) {
    return yearlyGoalRepository
        .findByUserIdAndYear(userId, year)
        .map(
            existing -> {
              log.info(
                  "Updating yearly goal userId={} year={} targetBooks={}",
                  userId,
                  year,
                  targetBooks);
              existing.updateTarget(targetBooks);
              return yearlyGoalRepository.save(existing);
            })
        .orElseGet(
            () -> {
              log.info(
                  "Creating yearly goal userId={} year={} targetBooks={}",
                  userId,
                  year,
                  targetBooks);
              final YearlyGoal newGoal = YearlyGoal.create(userId, year, targetBooks);
              return yearlyGoalRepository.save(newGoal);
            });
  }
}
