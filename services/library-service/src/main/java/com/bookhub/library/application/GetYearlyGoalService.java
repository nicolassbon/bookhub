package com.bookhub.library.application;

import com.bookhub.library.domain.YearlyGoal;
import com.bookhub.library.domain.YearlyGoalRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetYearlyGoalService {

  private final YearlyGoalRepository yearlyGoalRepository;

  @Transactional(readOnly = true)
  public Optional<YearlyGoal> execute(final UUID userId, final int year) {
    return yearlyGoalRepository.findByUserIdAndYear(userId, year);
  }
}
