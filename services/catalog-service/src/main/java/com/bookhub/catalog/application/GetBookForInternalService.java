package com.bookhub.catalog.application;

import com.bookhub.catalog.application.error.BookNotFoundException;
import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.domain.BookRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetBookForInternalService {

  private final BookRepository bookRepository;

  public GetBookForInternalService(final BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public Book getByIdOrThrow(final UUID bookId) {
    return bookRepository
        .findById(bookId)
        .orElseThrow(
            () -> new BookNotFoundException("Book not found: " + bookId));
  }
}
