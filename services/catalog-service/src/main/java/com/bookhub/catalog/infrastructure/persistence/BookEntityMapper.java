package com.bookhub.catalog.infrastructure.persistence;

import com.bookhub.catalog.application.support.BookNormalization;
import com.bookhub.catalog.domain.Book;
import org.springframework.stereotype.Component;

@Component
public class BookEntityMapper {

  public BookEntity toEntity(final Book book) {
    final BookEntity entity = new BookEntity();
    entity.setId(book.getId());
    entity.setTitle(book.getTitle());
    entity.setAuthorName(book.getAuthorName());
    entity.setIsbn13(BookNormalization.normalizeIsbn13(book.getIsbn13()));
    entity.setSourceReference(
        BookNormalization.normalizeSourceReference(book.getSourceReference()));
    entity.setCoverUrl(book.getCoverUrl());
    entity.setPublishedYear(book.getPublishedYear());
    entity.setPageCount(book.getPageCount());
    return entity;
  }

  public Book toDomain(final BookEntity entity) {
    return Book.builder()
        .id(entity.getId())
        .title(entity.getTitle())
        .authorName(entity.getAuthorName())
        .isbn13(entity.getIsbn13())
        .sourceReference(entity.getSourceReference())
        .coverUrl(entity.getCoverUrl())
        .publishedYear(entity.getPublishedYear())
        .pageCount(entity.getPageCount())
        .build();
  }
}
