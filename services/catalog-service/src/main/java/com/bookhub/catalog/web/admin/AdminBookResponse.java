package com.bookhub.catalog.web.admin;

import com.bookhub.catalog.domain.Book;
import java.time.Instant;
import java.util.UUID;

public record AdminBookResponse(
    UUID id,
    String title,
    String authorName,
    String isbn13,
    String sourceReference,
    String source,
    String coverUrl,
    Integer publishedYear,
    Integer pageCount,
    Instant createdAt) {

  public static AdminBookResponse from(final Book book) {
    return new AdminBookResponse(
        book.getId(),
        book.getTitle(),
        book.getAuthorName(),
        book.getIsbn13(),
        book.getSourceReference(),
        book.getSource(),
        book.getCoverUrl(),
        book.getPublishedYear(),
        book.getPageCount(),
        book.getCreatedAt());
  }
}
