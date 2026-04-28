package com.bookhub.catalog.web;

import com.bookhub.catalog.application.GetBookDetailService;
import com.bookhub.catalog.application.model.BookSearchItem;
import com.bookhub.catalog.domain.Book;
import org.springframework.stereotype.Component;

@Component
public class BookWebMapper {

  public BookSearchResponse toSearchResponse(final BookSearchItem item) {
    return BookSearchResponse.builder()
        .id(item.id())
        .title(item.title())
        .authorName(item.authorName())
        .coverUrl(item.coverUrl())
        .build();
  }

  public BookDetailResponse toDetailResponse(final Book book) {
    return BookDetailResponse.builder()
        .id(book.getId().toString())
        .title(book.getTitle())
        .authorName(book.getAuthorName())
        .isbn13(book.getIsbn13())
        .sourceReference(book.getSourceReference())
        .coverUrl(book.getCoverUrl())
        .publishedYear(book.getPublishedYear())
        .build();
  }

  public DegradedBookDetailResponse toDegradedDetailResponse(
      final GetBookDetailService.DegradedDetail degradedDetail) {
    return DegradedBookDetailResponse.builder()
        .id(degradedDetail.id())
        .code(degradedDetail.code())
        .message(degradedDetail.message())
        .degraded(true)
        .retryAfterSeconds(degradedDetail.retryAfterSeconds())
        .build();
  }
}
