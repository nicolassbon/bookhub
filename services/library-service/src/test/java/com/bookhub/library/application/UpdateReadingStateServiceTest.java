package com.bookhub.library.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhub.library.domain.BookSnapshot;
import com.bookhub.library.domain.ReadingState;
import com.bookhub.library.domain.UserBook;
import com.bookhub.library.domain.UserBookRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateReadingStateServiceTest {

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID ENTRY_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID CATALOG_BOOK_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000003");

  @Mock private UserBookRepository userBookRepository;
  @Mock private YearlyGoalProgressService yearlyGoalProgressService;
  @Mock private CreateNotificationService createNotificationService;

  @InjectMocks private UpdateReadingStateService service;

  @Test
  void shouldForwardPreviousFinishedAtWhenStateChangesToReadAgain() {
    final Instant previousFinishedAt = Instant.parse("2026-03-20T10:00:00Z");
    final UserBook userBook =
        UserBook.rehydrate(
            ENTRY_ID,
            USER_ID,
            CATALOG_BOOK_ID,
            new BookSnapshot("Clean Architecture", null, 300),
            ReadingState.READING,
            150,
            50,
            Instant.parse("2026-01-10T10:00:00Z"),
            previousFinishedAt,
            Instant.parse("2025-12-01T10:00:00Z"),
            Instant.parse("2026-03-01T10:00:00Z"));

    when(userBookRepository.findById(ENTRY_ID)).thenReturn(Optional.of(userBook));
    when(userBookRepository.save(userBook)).thenReturn(userBook);
    when(yearlyGoalProgressService.onBookCompleted(any(), any(), any(), any()))
        .thenReturn(Optional.empty());

    service.execute(USER_ID, ENTRY_ID, ReadingState.READ);

    verify(yearlyGoalProgressService)
        .onBookCompleted(
            eq(USER_ID), eq(CATALOG_BOOK_ID), eq(previousFinishedAt), any(Instant.class));
  }
}
