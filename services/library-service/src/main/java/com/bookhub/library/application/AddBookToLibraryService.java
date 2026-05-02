package com.bookhub.library.application;

import com.bookhub.library.application.error.BookNotFoundInCatalogException;
import com.bookhub.library.application.error.DuplicateLibraryEntryException;
import com.bookhub.library.domain.BookSnapshot;
import com.bookhub.library.domain.ReadingState;
import com.bookhub.library.domain.UserBook;
import com.bookhub.library.domain.UserBookRepository;
import com.bookhub.library.infrastructure.client.CatalogBook;
import com.bookhub.library.infrastructure.client.CatalogServiceClient;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddBookToLibraryService {

  private final UserBookRepository userBookRepository;
  private final CatalogServiceClient catalogServiceClient;

  @Transactional
  public UserBook execute(final UUID userId, final UUID bookId, final ReadingState initialState) {
    log.info(
        "Adding book to library userId={} bookId={} initialState={}", userId, bookId, initialState);

    final CatalogBook catalogBook =
        catalogServiceClient
            .findBookById(bookId)
            .orElseThrow(
                () -> new BookNotFoundInCatalogException("Book not found in catalog: " + bookId));

    userBookRepository
        .findByUserIdAndBookId(userId, bookId)
        .ifPresent(
            existing -> {
              throw new DuplicateLibraryEntryException(
                  "Book " + bookId + " is already in your library");
            });

    final UserBook userBook =
        UserBook.create(
            userId,
            bookId,
            initialState,
            new BookSnapshot(catalogBook.title(), catalogBook.coverUrl(), catalogBook.pageCount()));
    final UserBook saved = userBookRepository.save(userBook);

    log.info("Book added to library entryId={} userId={} bookId={}", saved.getId(), userId, bookId);
    return saved;
  }
}
