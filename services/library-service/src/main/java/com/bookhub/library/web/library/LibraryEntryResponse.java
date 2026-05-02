package com.bookhub.library.web.library;

import com.bookhub.library.domain.ReadingState;
import com.bookhub.library.domain.UserBook;
import java.time.Instant;
import java.util.UUID;

public record LibraryEntryResponse(
    UUID entryId,
    UUID userId,
    UUID bookId,
    BookResponse book,
    ReadingState state,
    ProgressResponse progress,
    Instant startedAt,
    Instant finishedAt,
    Instant addedAt) {

  public record ProgressResponse(int pagesRead, Integer percentage) {}

  public record BookResponse(String title, String coverUrl, Integer pageCount) {}

  public static LibraryEntryResponse from(final UserBook userBook) {
    return new LibraryEntryResponse(
        userBook.getId(),
        userBook.getUserId(),
        userBook.getBookId(),
        new BookResponse(
            userBook.getBook().title(),
            userBook.getBook().coverUrl(),
            userBook.getBook().pageCount()),
        userBook.getState(),
        new ProgressResponse(userBook.getPagesRead(), userBook.getPercentage()),
        userBook.getStartedAt(),
        userBook.getFinishedAt(),
        userBook.getAddedAt());
  }
}
