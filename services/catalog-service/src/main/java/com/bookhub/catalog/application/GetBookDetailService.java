package com.bookhub.catalog.application;

import com.bookhub.catalog.application.error.BookNotFoundException;
import com.bookhub.catalog.application.error.ExternalProviderException;
import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.domain.BookIdentifier;
import com.bookhub.catalog.domain.BookRepository;
import com.bookhub.catalog.domain.SearchProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetBookDetailService {

    private final BookRepository bookRepository;
    private final SearchProvider searchProvider;

    public GetBookDetailService(final BookRepository bookRepository, final SearchProvider searchProvider) {
        this.bookRepository = bookRepository;
        this.searchProvider = searchProvider;
    }

    @Transactional
    public Book getById(final String rawId) {
        final BookIdentifier bookIdentifier = BookIdentifier.parse(rawId);
        if (bookIdentifier.isLocal()) {
            return bookRepository.findById(bookIdentifier.localId())
                    .orElseThrow(() -> new BookNotFoundException("Book not found: " + rawId));
        }

        return getOrImportExternal(bookIdentifier.sourceReference());
    }

    private Book getOrImportExternal(final String sourceReference) {
        final String normalizedSourceReference = sourceReference.trim().toUpperCase();
        return bookRepository.findBySourceReference(normalizedSourceReference)
                .orElseGet(() -> fetchAndPersist(normalizedSourceReference));
    }

    private Book fetchAndPersist(final String sourceReference) {
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

            return bookRepository.save(normalizedBook);
        } catch (DataIntegrityViolationException exception) {
            return bookRepository.findBySourceReference(sourceReference)
                    .orElseThrow(() -> new BookNotFoundException("Book not found after concurrent import"));
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
}
