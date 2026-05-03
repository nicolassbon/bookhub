package com.bookhub.library.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhub.library.domain.GoalStatus;
import com.bookhub.library.domain.YearlyGoal;
import com.bookhub.library.domain.YearlyGoalRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManageYearlyGoalServiceTest {

  @Mock private YearlyGoalRepository yearlyGoalRepository;

  @InjectMocks private ManageYearlyGoalService service;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Nested
  class WhenGoalDoesNotExist {

    @Test
    void shouldCreateNewGoal() {
      when(yearlyGoalRepository.findByUserIdAndYear(USER_ID, 2026)).thenReturn(Optional.empty());
      when(yearlyGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

      final YearlyGoal result = service.execute(USER_ID, 2026, 24);

      assertThat(result.getTargetBooks()).isEqualTo(24);
      assertThat(result.getYear()).isEqualTo(2026);
      assertThat(result.getStatus()).isEqualTo(GoalStatus.IN_PROGRESS);

      final ArgumentCaptor<YearlyGoal> captor = ArgumentCaptor.forClass(YearlyGoal.class);
      verify(yearlyGoalRepository).save(captor.capture());
      assertThat(captor.getValue().getTargetBooks()).isEqualTo(24);
    }
  }

  @Nested
  class WhenGoalAlreadyExists {

    @Test
    void shouldUpdateTargetOnExistingGoal() {
      final YearlyGoal existing = YearlyGoal.create(USER_ID, 2026, 12);
      when(yearlyGoalRepository.findByUserIdAndYear(USER_ID, 2026))
          .thenReturn(Optional.of(existing));
      when(yearlyGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

      final YearlyGoal result = service.execute(USER_ID, 2026, 36);

      assertThat(result.getTargetBooks()).isEqualTo(36);
      // Verify it updated the same object, not created a new one
      verify(yearlyGoalRepository).save(existing);
    }

    @Test
    void shouldNotCreateDuplicate() {
      final YearlyGoal existing = YearlyGoal.create(USER_ID, 2026, 12);
      when(yearlyGoalRepository.findByUserIdAndYear(USER_ID, 2026))
          .thenReturn(Optional.of(existing));
      when(yearlyGoalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

      service.execute(USER_ID, 2026, 20);

      // save called once (update), never with a new object
      verify(yearlyGoalRepository).save(existing);
    }
  }
}
