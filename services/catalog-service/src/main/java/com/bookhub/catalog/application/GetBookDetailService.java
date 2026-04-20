package com.bookhub.catalog.application;

import com.bookhub.catalog.application.error.BookNotFoundException;
import com.bookhub.catalog.application.error.ExternalProviderException;
import com.bookhub.catalog.application.error.ExternalServiceUnavailableException;
import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.domain.BookIdentifier;
import com.bookhub.catalog.domain.BookRepository;
import com.bookhub.catalog.domain.SearchProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetBookDetailService {

    private static final String DEGRADED_CODE = "OPENLIBRARY_UNAVAILABLE";
    private static final String DEGRADED_MESSAGE = "OpenLibrary is temporarily unavailable. Please retry later.";

    private final BookRepository bookRepository;
    private final SearchProvider searchProvider;
    private final int defaultRetryAfterSeconds;

    @Autowired
    public GetBookDetailService(final BookRepository bookRepository, final SearchProvider searchProvider) {
        this(bookRepository, searchProvider, 30);
    }

    GetBookDetailService(
            final BookRepository bookRepository,
            final SearchProvider searchProvider,
            final int defaultRetryAfterSeconds) {
        this.bookRepository = bookRepository;
        this.searchProvider = searchProvider;
        this.defaultRetryAfterSeconds = defaultRetryAfterSeconds;
    }

    @Transactional
    public BookDetailResult getById(final String rawId) {
        final BookIdentifier bookIdentifier = BookIdentifier.parse(rawId);
        if (bookIdentifier.isLocal()) {
            final Book localBook = bookRepository.findById(bookIdentifier.localId())
                    .orElseThrow(() -> new BookNotFoundException("Book not found: " + rawId));
            return BookDetailResult.success(localBook);
        }

        return getOrImportExternal(rawId, bookIdentifier.sourceReference());
    }

    private BookDetailResult getOrImportExternal(final String rawId, final String sourceReference) {
        final String normalizedSourceReference = sourceReference.trim().toUpperCase();
        return bookRepository.findBySourceReference(normalizedSourceReference)
                .map(BookDetailResult::success)
                .orElseGet(() -> fetchAndPersist(rawId, normalizedSourceReference));
    }

    private BookDetailResult fetchAndPersist(final String rawId, final String sourceReference) {
        try {
            final Book fetchedBook = searchProvider.fetchDetail(sourceReference);
            final Book normalizedBook = Book.builder()
                    .title(fetchedBook.getTitle())
                    .authorName(normalizeAuthorName(fetchedBook.getAuthorName()))
                    .isbn13(fetchedBook.getIsbn13())
                    .sourceReference(sourceReference)
                    .coverUrl(fetchedBook.getCoverUrl())
                    .publishedYear(fetchedBook.getPublishedYear())
                    .build();

            return BookDetailResult.success(bookRepository.save(normalizedBook));
        } catch (DataIntegrityViolationException exception) {
            final Book persistedBook = bookRepository.findBySourceReference(sourceReference)
                    .orElseThrow(() -> new BookNotFoundException("Book not found after concurrent import"));
            return BookDetailResult.success(persistedBook);
        } catch (ExternalServiceUnavailableException exception) {
            final int retryAfterSeconds = exception.retryAfterSeconds() > 0
                    ? exception.retryAfterSeconds()
                    : defaultRetryAfterSeconds;
            return BookDetailResult.degraded(new DegradedDetail(
                    rawId,
                    DEGRADED_CODE,
                    DEGRADED_MESSAGE,
                    retryAfterSeconds));
        } catch (ExternalProviderException exception) {
            throw exception;
        }
    }

    private String normalizeAuthorName(final String authorName) {
        if (authorName == null || authorName.isBlank()) {
            return "Unknown";
        }
        return authorName;
    }

    public record BookDetailResult(Book book, DegradedDetail degraded) {

        public static BookDetailResult success(final Book book) {
            return new BookDetailResult(book, null);
        }

        public static BookDetailResult degraded(final DegradedDetail degraded) {
            return new BookDetailResult(null, degraded);
        }

        public boolean isDegraded() {
            return degraded != null;
        }
    }

    public record DegradedDetail(
            String id,
            String code,
            String message,
            Integer retryAfterSeconds) {
    }
}
