package com.bookhub.library.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class UserBook {

  private UUID id;
  private final UUID userId;
  private final UUID bookId;
  private final BookSnapshot book;
  private ReadingState state;
  private int pagesRead;
  private Integer percentage;
  private Instant startedAt;
  private Instant finishedAt;
  private final Instant addedAt;
  private Instant lastProgressAt;

  private UserBook(
      final UUID id,
      final UUID userId,
      final UUID bookId,
      final BookSnapshot book,
      final ReadingState state,
      final int pagesRead,
      final Integer percentage,
      final Instant startedAt,
      final Instant finishedAt,
      final Instant addedAt,
      final Instant lastProgressAt) {
    this.id = id;
    this.userId = userId;
    this.bookId = bookId;
    this.book = book;
    this.state = state;
    this.pagesRead = pagesRead;
    this.percentage = percentage;
    this.startedAt = startedAt;
    this.finishedAt = finishedAt;
    this.addedAt = addedAt;
    this.lastProgressAt = lastProgressAt;
  }

  /** Factory for creating a new library entry. */
  public static UserBook create(
      final UUID userId,
      final UUID bookId,
      final ReadingState initialState,
      final BookSnapshot bookSnapshot) {
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(bookId, "bookId must not be null");
    Objects.requireNonNull(bookSnapshot, "bookSnapshot must not be null");
    final ReadingState state = initialState != null ? initialState : ReadingState.WANT_TO_READ;
    final Instant now = Instant.now();
    final Instant startedAt = state == ReadingState.READING ? now : null;
    final Integer percentage = bookSnapshot.pageCount() == null ? null : 0;
    return new UserBook(
        null, userId, bookId, bookSnapshot, state, 0, percentage, startedAt, null, now, null);
  }

  /** Rehydrates from persistence. */
  public static UserBook rehydrate(
      final UUID id,
      final UUID userId,
      final UUID bookId,
      final BookSnapshot book,
      final ReadingState state,
      final int pagesRead,
      final Integer percentage,
      final Instant startedAt,
      final Instant finishedAt,
      final Instant addedAt,
      final Instant lastProgressAt) {
    return new UserBook(
        id,
        userId,
        bookId,
        book,
        state,
        pagesRead,
        percentage,
        startedAt,
        finishedAt,
        addedAt,
        lastProgressAt);
  }

  /** Updates the reading state with valid transition rules. */
  public void updateState(final ReadingState newState) {
    Objects.requireNonNull(newState, "state must not be null");
    if (this.state == newState) {
      return;
    }

    final Instant now = Instant.now();

    if (newState == ReadingState.READING && this.startedAt == null) {
      this.startedAt = now;
    }

    if (newState == ReadingState.READ) {
      normalizeProgressToCompletion();
      this.finishedAt = now;
      if (this.startedAt == null) {
        this.startedAt = now;
      }
    }

    this.state = newState;
  }

  /**
   * Updates reading progress. Recalculates percentage based on total pages. Auto-transitions
   * WANT_TO_READ → READING on first progress update.
   */
  public void updateProgress(final int newPagesRead) {
    if (newPagesRead < 0) {
      throw new ReadingProgressException("pagesRead cannot be negative");
    }

    final Integer totalPages = book.pageCount();
    if (totalPages != null && totalPages > 0 && newPagesRead > totalPages) {
      throw new ReadingProgressException(
          "pagesRead (" + newPagesRead + ") cannot exceed totalPages (" + totalPages + ")");
    }

    this.pagesRead = newPagesRead;
    this.lastProgressAt = Instant.now();

    if (totalPages == null) {
      this.percentage = null;
    } else if (totalPages > 0) {
      this.percentage = Math.min(100, (int) ((long) newPagesRead * 100 / totalPages));
    }

    // Auto-transition: first progress update moves WANT_TO_READ → READING
    if (this.state == ReadingState.WANT_TO_READ && newPagesRead > 0) {
      updateState(ReadingState.READING);
    }

    // Auto-transition: 100% → READ
    if (Integer.valueOf(100).equals(this.percentage) && this.state != ReadingState.READ) {
      updateState(ReadingState.READ);
    }

    if (this.state == ReadingState.READ && (this.percentage == null || this.percentage < 100)) {
      updateState(ReadingState.READING);
    }
  }

  private void normalizeProgressToCompletion() {
    if (book.pageCount() == null) {
      return;
    }

    this.pagesRead = book.pageCount();
    this.percentage = 100;
    this.lastProgressAt = Instant.now();
  }

  public boolean isOwnedBy(final UUID userId) {
    return this.userId.equals(userId);
  }

  public UUID getId() {
    return id;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getBookId() {
    return bookId;
  }

  public BookSnapshot getBook() {
    return book;
  }

  public ReadingState getState() {
    return state;
  }

  public int getPagesRead() {
    return pagesRead;
  }

  public Integer getPercentage() {
    return percentage;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getFinishedAt() {
    return finishedAt;
  }

  public Instant getAddedAt() {
    return addedAt;
  }

  public Instant getLastProgressAt() {
    return lastProgressAt;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof UserBook userBook)) {
      return false;
    }
    return id != null && Objects.equals(id, userBook.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
