package com.bookhub.catalog.application.admin;

import com.bookhub.catalog.application.error.BookNotFoundException;
import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.domain.BookRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UpdateBookService {

  private final BookRepository bookRepository;

  public UpdateBookService(final BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public Book update(final UUID bookId, final UpdateBookCommand command) {
    final Book existing =
        bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId.toString()));

    final Book updated =
        Book.builder()
            .id(existing.getId())
            .title(command.title() != null ? command.title() : existing.getTitle())
            .authorName(command.authorName() != null ? command.authorName() : existing.getAuthorName())
            .isbn13(command.isbn13() != null ? command.isbn13() : existing.getIsbn13())
            .sourceReference(existing.getSourceReference())
            .coverUrl(command.coverUrl() != null ? command.coverUrl() : existing.getCoverUrl())
            .publishedYear(
                command.publishedYear() != null ? command.publishedYear() : existing.getPublishedYear())
            .pageCount(command.pageCount() != null ? command.pageCount() : existing.getPageCount())
            .createdAt(existing.getCreatedAt())
            .build();

    return bookRepository.save(updated);
  }

  public record UpdateBookCommand(
      String title,
      String authorName,
      String isbn13,
      String coverUrl,
      Integer publishedYear,
      Integer pageCount) {}
}
