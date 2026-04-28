package com.bookhub.catalog.web.internal;

import com.bookhub.catalog.application.GetBookForInternalService;
import com.bookhub.catalog.domain.Book;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/books")
public class InternalBookController {

  private final GetBookForInternalService getBookForInternalService;

  public InternalBookController(final GetBookForInternalService getBookForInternalService) {
    this.getBookForInternalService = getBookForInternalService;
  }

  @GetMapping("/{bookId}")
  public InternalBookResponse getBook(@PathVariable final UUID bookId) {
    final Book book = getBookForInternalService.getByIdOrThrow(bookId);
    return new InternalBookResponse(
        book.getId(), book.getTitle(), book.getCoverUrl(), book.getPageCount(), true, "ACTIVE");
  }
}
