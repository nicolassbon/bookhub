package com.bookhub.library.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserBookTest {

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("Should create with default WANT_TO_READ state")
    void shouldCreateWithDefaultState() {
      final UserBook userBook =
          UserBook.create(USER_ID, BOOK_ID, null, new BookSnapshot("Clean Code", null, 464));

      assertThat(userBook.getState()).isEqualTo(ReadingState.WANT_TO_READ);
      assertThat(userBook.getPagesRead()).isZero();
      assertThat(userBook.getPercentage()).isZero();
      assertThat(userBook.getBook().title()).isEqualTo("Clean Code");
      assertThat(userBook.getStartedAt()).isNull();
      assertThat(userBook.getFinishedAt()).isNull();
      assertThat(userBook.getAddedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should create with READING state and set startedAt")
    void shouldCreateWithReadingStateAndSetStartedAt() {
      final UserBook userBook =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.READING, new BookSnapshot("Clean Code", null, 464));

      assertThat(userBook.getState()).isEqualTo(ReadingState.READING);
      assertThat(userBook.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should create with explicit WANT_TO_READ state")
    void shouldCreateWithExplicitWantToReadState() {
      final UserBook userBook =
          UserBook.create(
              USER_ID,
              BOOK_ID,
              ReadingState.WANT_TO_READ,
              new BookSnapshot("Clean Code", null, 464));

      assertThat(userBook.getState()).isEqualTo(ReadingState.WANT_TO_READ);
      assertThat(userBook.getStartedAt()).isNull();
    }
  }

  @Nested
  @DisplayName("updateState")
  class UpdateState {

    @Test
    @DisplayName("Should transition from WANT_TO_READ to READING")
    void shouldTransitionToReading() {
      final UserBook userBook =
          UserBook.create(
              USER_ID,
              BOOK_ID,
              ReadingState.WANT_TO_READ,
              new BookSnapshot("Clean Code", null, 464));
      userBook.updateState(ReadingState.READING);

      assertThat(userBook.getState()).isEqualTo(ReadingState.READING);
      assertThat(userBook.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should transition from READING to READ and set finishedAt")
    void shouldTransitionToReadAndSetFinishedAt() {
      final UserBook userBook =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.READING, new BookSnapshot("Clean Code", null, 464));
      userBook.updateState(ReadingState.READ);

      assertThat(userBook.getState()).isEqualTo(ReadingState.READ);
      assertThat(userBook.getFinishedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should set startedAt when transitioning directly to READ")
    void shouldSetStartedAtWhenTransitioningDirectlyToRead() {
      final UserBook userBook =
          UserBook.create(
              USER_ID,
              BOOK_ID,
              ReadingState.WANT_TO_READ,
              new BookSnapshot("Clean Code", null, 464));
      userBook.updateState(ReadingState.READ);

      assertThat(userBook.getState()).isEqualTo(ReadingState.READ);
      assertThat(userBook.getStartedAt()).isNotNull();
      assertThat(userBook.getFinishedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should be idempotent for same state")
    void shouldBeIdempotentForSameState() {
      final UserBook userBook =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.READING, new BookSnapshot("Clean Code", null, 464));
      userBook.updateState(ReadingState.READING);

      assertThat(userBook.getState()).isEqualTo(ReadingState.READING);
    }

    @Test
    @DisplayName("Should normalize progress to completion when manually setting READ")
    void shouldNormalizeProgressWhenManuallyMarkingRead() {
      final UserBook userBook =
          UserBook.create(
              USER_ID,
              BOOK_ID,
              ReadingState.WANT_TO_READ,
              new BookSnapshot("Clean Code", null, 464));

      userBook.updateProgress(120);
      userBook.updateState(ReadingState.READ);

      assertThat(userBook.getPagesRead()).isEqualTo(464);
      assertThat(userBook.getPercentage()).isEqualTo(100);
      assertThat(userBook.getState()).isEqualTo(ReadingState.READ);
    }

    @Test
    @DisplayName("Should allow READ to READING transition for reread")
    void shouldAllowReadToReadingTransition() {
      final UserBook userBook =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.READING, new BookSnapshot("Clean Code", null, 464));

      userBook.updateProgress(464);
      userBook.updateState(ReadingState.READING);

      assertThat(userBook.getState()).isEqualTo(ReadingState.READING);
    }
  }

  @Nested
  @DisplayName("updateProgress")
  class UpdateProgress {

    @Test
    @DisplayName("Should calculate percentage from pages read and total")
    void shouldCalculatePercentage() {
      final UserBook userBook =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.READING, new BookSnapshot("Clean Code", null, 400));
      userBook.updateProgress(120);

      assertThat(userBook.getPagesRead()).isEqualTo(120);
      assertThat(userBook.getPercentage()).isEqualTo(30);
      assertThat(userBook.getLastProgressAt()).isNotNull();
    }

    @Test
    @DisplayName("Should auto-transition WANT_TO_READ to READING on first progress")
    void shouldAutoTransitionToReading() {
      final UserBook userBook =
          UserBook.create(
              USER_ID,
              BOOK_ID,
              ReadingState.WANT_TO_READ,
              new BookSnapshot("Clean Code", null, 400));
      userBook.updateProgress(10);

      assertThat(userBook.getState()).isEqualTo(ReadingState.READING);
      assertThat(userBook.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should auto-transition to READ at 100 percent")
    void shouldAutoTransitionToReadAtFullProgress() {
      final UserBook userBook =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.READING, new BookSnapshot("Clean Code", null, 400));
      userBook.updateProgress(400);

      assertThat(userBook.getState()).isEqualTo(ReadingState.READ);
      assertThat(userBook.getPercentage()).isEqualTo(100);
      assertThat(userBook.getFinishedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should not auto-transition when pages read is zero")
    void shouldNotAutoTransitionWhenPagesReadIsZero() {
      final UserBook userBook =
          UserBook.create(
              USER_ID,
              BOOK_ID,
              ReadingState.WANT_TO_READ,
              new BookSnapshot("Clean Code", null, 400));
      userBook.updateProgress(0);

      assertThat(userBook.getState()).isEqualTo(ReadingState.WANT_TO_READ);
    }

    @Test
    @DisplayName("Should keep percentage null when page count is unknown")
    void shouldKeepPercentageNullWhenPageCountUnknown() {
      final UserBook userBook =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.READING, new BookSnapshot("Unknown", null, null));
      userBook.updateProgress(50);

      assertThat(userBook.getPagesRead()).isEqualTo(50);
      assertThat(userBook.getPercentage()).isNull();
    }

    @Test
    @DisplayName("Should reject negative pages read")
    void shouldRejectNegativePagesRead() {
      final UserBook userBook =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.READING, new BookSnapshot("Clean Code", null, 400));

      assertThatThrownBy(() -> userBook.updateProgress(-1))
          .isInstanceOf(ReadingProgressException.class)
          .hasMessageContaining("negative");
    }

    @Test
    @DisplayName("Should reject pages read exceeding total pages")
    void shouldRejectPagesReadExceedingTotal() {
      final UserBook userBook =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.READING, new BookSnapshot("Clean Code", null, 400));

      assertThatThrownBy(() -> userBook.updateProgress(500))
          .isInstanceOf(ReadingProgressException.class)
          .hasMessageContaining("exceed");
    }

    @Test
    @DisplayName("Should reopen to READING when progress drops below completion")
    void shouldReopenToReadingWhenProgressDropsBelowCompletion() {
      final UserBook userBook =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.READING, new BookSnapshot("Clean Code", null, 400));

      userBook.updateProgress(400);
      userBook.updateProgress(320);

      assertThat(userBook.getState()).isEqualTo(ReadingState.READING);
      assertThat(userBook.getPercentage()).isEqualTo(80);
    }
  }

  @Nested
  @DisplayName("ownership")
  class Ownership {

    @Test
    @DisplayName("Should confirm ownership for matching userId")
    void shouldConfirmOwnership() {
      final UserBook userBook =
          UserBook.create(USER_ID, BOOK_ID, null, new BookSnapshot("Clean Code", null, 464));
      assertThat(userBook.isOwnedBy(USER_ID)).isTrue();
    }

    @Test
    @DisplayName("Should deny ownership for different userId")
    void shouldDenyOwnership() {
      final UserBook userBook =
          UserBook.create(USER_ID, BOOK_ID, null, new BookSnapshot("Clean Code", null, 464));
      final UUID otherUser = UUID.fromString("00000000-0000-0000-0000-000000000099");
      assertThat(userBook.isOwnedBy(otherUser)).isFalse();
    }
  }
}
