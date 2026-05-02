package com.bookhub.library.web.library;

import com.bookhub.library.domain.ReadingState;
import com.bookhub.library.domain.UserBook;
import java.util.UUID;

public record UpdateProgressResponse(
    UUID entryId, int pagesRead, Integer percentage, ReadingState state) {

  public static UpdateProgressResponse from(final UserBook userBook) {
    return new UpdateProgressResponse(
        userBook.getId(), userBook.getPagesRead(), userBook.getPercentage(), userBook.getState());
  }
}
