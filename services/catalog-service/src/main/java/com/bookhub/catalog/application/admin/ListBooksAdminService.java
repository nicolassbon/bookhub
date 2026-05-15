package com.bookhub.catalog.application.admin;

import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.domain.BookRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListBooksAdminService {

  private final BookRepository bookRepository;

  public ListBooksAdminService(final BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public PagedBooksResult list(final int page, final int size) {
    final List<Book> books = bookRepository.findAll(page, size);
    final long totalElements = bookRepository.countAll();
    final long totalPages =
        totalElements == 0 ? 0 : (long) Math.ceil((double) totalElements / size);
    return new PagedBooksResult(books, page, size, totalElements, totalPages);
  }

  public PagedBooksResult list(final int page, final int size, final String source) {
    final List<Book> books = bookRepository.findAll(page, size, source);
    final long totalElements = bookRepository.countAll(source);
    final long totalPages =
        totalElements == 0 ? 0 : (long) Math.ceil((double) totalElements / size);
    return new PagedBooksResult(books, page, size, totalElements, totalPages);
  }

  public record PagedBooksResult(
      List<Book> books, int page, int size, long totalElements, long totalPages) {}
}
