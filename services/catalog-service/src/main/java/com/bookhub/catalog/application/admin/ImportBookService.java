package com.bookhub.catalog.application.admin;

import com.bookhub.catalog.application.GetBookDetailService;
import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.domain.BookRepository;
import org.springframework.stereotype.Service;

@Service
public class ImportBookService {

  private final GetBookDetailService getBookDetailService;
  private final BookRepository bookRepository;

  public ImportBookService(
      final GetBookDetailService getBookDetailService, final BookRepository bookRepository) {
    this.getBookDetailService = getBookDetailService;
    this.bookRepository = bookRepository;
  }

  public ImportBookResult importBook(final ImportBookCommand command) {
    final boolean existed =
        bookRepository.findBySourceReference(command.sourceReference()).isPresent();

    final GetBookDetailService.BookDetailResult result =
        getBookDetailService.getById(command.sourceReference());

    if (result.isDegraded()) {
      throw new AdminImportDegradedException(result.degraded().message());
    }

    return new ImportBookResult(result.book(), existed);
  }

  public record ImportBookCommand(String sourceReference, String provider) {}

  public record ImportBookResult(Book book, boolean existed) {}
}
