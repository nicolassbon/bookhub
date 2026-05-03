package com.bookhub.library.application;

import com.bookhub.library.application.error.LibraryEntryNotFoundException;
import com.bookhub.library.application.error.LibraryEntryOwnershipException;
import com.bookhub.library.domain.NotificationType;
import com.bookhub.library.domain.ReadingState;
import com.bookhub.library.domain.UserBook;
import com.bookhub.library.domain.UserBookRepository;
import com.bookhub.library.domain.YearlyGoal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateReadingStateService {

  private final UserBookRepository userBookRepository;
  private final YearlyGoalProgressService yearlyGoalProgressService;
  private final CreateNotificationService createNotificationService;

  @Transactional
  public UserBook execute(final UUID userId, final UUID entryId, final ReadingState newState) {
    log.info("Updating reading state entryId={} userId={} newState={}", entryId, userId, newState);

    final UserBook userBook =
        userBookRepository
            .findById(entryId)
            .orElseThrow(
                () -> new LibraryEntryNotFoundException("Library entry not found: " + entryId));

    if (!userBook.isOwnedBy(userId)) {
      throw new LibraryEntryOwnershipException("User " + userId + " does not own entry " + entryId);
    }

    final ReadingState stateBefore = userBook.getState();
    final Instant finishedAtBefore = userBook.getFinishedAt();
    userBook.updateState(newState);
    final UserBook saved = userBookRepository.save(userBook);

    if (stateBefore != ReadingState.READ && saved.getState() == ReadingState.READ) {
      final Optional<YearlyGoal> goal =
          yearlyGoalProgressService.onBookCompleted(
              userId, saved.getBookId(), finishedAtBefore, saved.getFinishedAt());

      if (goal.isPresent()) {
        final YearlyGoal g = goal.get();
        if (g.getCompletedBooks() == g.getTargetBooks()) {
          createNotificationService.execute(
              userId,
              NotificationType.GOAL_ACHIEVED,
              "Goal Achieved! \uD83C\uDF89",
              "You reached your goal of " + g.getTargetBooks() + " books this year!",
              "{\"year\":" + g.getYear() + "}");
        } else {
          createNotificationService.execute(
              userId,
              NotificationType.GOAL_PROGRESS,
              "Book Finished! \uD83D\uDCDA",
              "You've read "
                  + g.getCompletedBooks()
                  + " out of "
                  + g.getTargetBooks()
                  + " books this year.",
              "{\"year\":" + g.getYear() + "}");
        }
      } else {
        createNotificationService.execute(
            userId,
            NotificationType.SYSTEM,
            "Book Finished! \uD83D\uDCDA",
            "Congratulations on finishing another book!",
            "{}");
      }
    }

    return saved;
  }
}
