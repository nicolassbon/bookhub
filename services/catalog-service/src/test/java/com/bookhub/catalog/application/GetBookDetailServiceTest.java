package com.bookhub.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;

import com.bookhub.catalog.application.error.BookNotFoundException;
import com.bookhub.catalog.application.error.InvalidBookIdException;
import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.domain.BookRepository;
import com.bookhub.catalog.domain.SearchProvider;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class GetBookDetailServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private SearchProvider searchProvider;

    private GetBookDetailService getBookDetailService;

    @BeforeEach
    void setUp() {
        getBookDetailService = new GetBookDetailService(bookRepository, searchProvider);
    }

    @Test
    void shouldFetchAndPersistWhenBookIdIsEphemeral() {
        final Book fetched = Book.builder()
                .title("The Hobbit")
                .authorName("J.R.R. Tolkien")
                .isbn13("9780261103344")
                .publishedYear(1937)
                .sourceReference("OL262758W")
                .build();
        final Book persisted = Book.builder()
                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .title("The Hobbit")
                .sourceReference("OL262758W")
                .build();

        when(bookRepository.findBySourceReference("OL262758W")).thenReturn(Optional.empty());
        when(searchProvider.fetchDetail("OL262758W")).thenReturn(fetched);
        when(bookRepository.save(any(Book.class))).thenReturn(persisted);

        final Book result = getBookDetailService.getById("ext:ol:OL262758W");

        assertThat(result.getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        verify(bookRepository).save(argThat(book ->
                "J.R.R. Tolkien".equals(book.getAuthorName())
                        && "9780261103344".equals(book.getIsbn13())
                        && Integer.valueOf(1937).equals(book.getPublishedYear())));
    }

    @Test
    void shouldApplySafeDefaultsWhenFetchedDetailIsMalformed() {
        final Book fetched = Book.builder()
                .title("Unknown Work")
                .authorName(null)
                .isbn13(null)
                .publishedYear(null)
                .sourceReference("OLMALFORMEDW")
                .build();

        final Book persisted = Book.builder()
                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"))
                .title("Unknown Work")
                .authorName("Unknown")
                .isbn13(null)
                .publishedYear(null)
                .sourceReference("OLMALFORMEDW")
                .build();

        when(bookRepository.findBySourceReference("OLMALFORMEDW")).thenReturn(Optional.empty());
        when(searchProvider.fetchDetail("OLMALFORMEDW")).thenReturn(fetched);
        when(bookRepository.save(any(Book.class))).thenReturn(persisted);

        final Book result = getBookDetailService.getById("ext:ol:OLMALFORMEDW");

        assertThat(result.getAuthorName()).isEqualTo("Unknown");
        verify(bookRepository).save(argThat(book ->
                "Unknown".equals(book.getAuthorName())
                        && book.getIsbn13() == null
                        && book.getPublishedYear() == null));
    }

    @Test
    void shouldReturnPersistedBookWhenDuplicateInsertHappensOnConcurrentRequests() {
        final Book fetched = Book.builder()
                .title("The Hobbit")
                .sourceReference("OL262758W")
                .build();
        final Book persisted = Book.builder()
                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .title("The Hobbit")
                .sourceReference("OL262758W")
                .build();

        when(bookRepository.findBySourceReference("OL262758W"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(persisted));
        when(searchProvider.fetchDetail("OL262758W")).thenReturn(fetched);
        when(bookRepository.save(any(Book.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        final Book result = getBookDetailService.getById("ext:ol:OL262758W");

        assertThat(result.getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    }

    @Test
    void shouldShortCircuitExternalLookupWhenBookIdIsLocalUuid() {
        final UUID bookId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        final Book persisted = Book.builder()
                .id(bookId)
                .title("The Hobbit")
                .sourceReference("OL262758W")
                .build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(persisted));

        final Book result = getBookDetailService.getById(bookId.toString());

        assertThat(result.getId()).isEqualTo(bookId);
        verify(searchProvider, never()).fetchDetail(any());
    }

    @Test
    void shouldRejectInvalidBookIdFormat() {
        assertThatThrownBy(() -> getBookDetailService.getById("invalid-id"))
                .isInstanceOf(InvalidBookIdException.class);
    }

    @Test
    void shouldFailWhenBookDoesNotExist() {
        final UUID bookId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getBookDetailService.getById(bookId.toString()))
                .isInstanceOf(BookNotFoundException.class);
    }
}
