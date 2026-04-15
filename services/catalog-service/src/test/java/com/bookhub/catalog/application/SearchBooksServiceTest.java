package com.bookhub.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.bookhub.catalog.application.model.BookSearchItem;
import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.domain.BookRepository;
import com.bookhub.catalog.domain.SearchProvider;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class SearchBooksServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private SearchProvider searchProvider;

    private SearchBooksService searchBooksService;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        final Executor directExecutor = Runnable::run;
        meterRegistry = new SimpleMeterRegistry();
        searchBooksService = new SearchBooksService(
                bookRepository,
                searchProvider,
                Duration.ofMillis(100),
                directExecutor,
                meterRegistry);
    }

    void shouldMergeLocalAndExternalResultsConcurrently() {
        final Book localBook = Book.builder()
                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .title("The Hobbit")
                .authorName("J.R.R. Tolkien")
                .sourceReference("OL262758W")
                .isbn13("9780261103344")
                .build();

        final BookSearchItem external = new BookSearchItem(
                "OL262758W",
                "The Hobbit",
                "J.R.R. Tolkien",
                "OL262758W",
                "9780261103344",
                null,
                null);

        when(bookRepository.searchByQuery(eq("hobbit"), any(PageRequest.class)))
                .thenReturn(List.of(localBook));
        when(searchProvider.searchAsync(eq("hobbit"), eq(20), any()))
                .thenReturn(CompletableFuture.completedFuture(List.of(external)));

        final List<BookSearchItem> result = searchBooksService.search("hobbit");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
    }

    @Test
    void shouldFallbackToLocalResultsWhenExternalTimesOut() {
        final Book localBook = Book.builder()
                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .title("The Hobbit")
                .authorName("J.R.R. Tolkien")
                .sourceReference("OL262758W")
                .isbn13("9780261103344")
                .build();

        when(bookRepository.searchByQuery(eq("hobbit"), any(PageRequest.class)))
                .thenReturn(List.of(localBook));
        when(searchProvider.searchAsync(eq("hobbit"), eq(20), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("timeout")));

        final List<BookSearchItem> result = searchBooksService.search("hobbit");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
    }

    @Test
    void shouldExposeEphemeralIdForUnpersistedExternalBooks() {
        final BookSearchItem external = new BookSearchItem(
                "OL82563W",
                "The Silmarillion",
                "J.R.R. Tolkien",
                "OL82563W",
                "9780261102736",
                null,
                null);

        when(bookRepository.searchByQuery(eq("silmarillion"), any(PageRequest.class)))
                .thenReturn(List.of());
        when(searchProvider.searchAsync(eq("silmarillion"), eq(20), any()))
                .thenReturn(CompletableFuture.completedFuture(List.of(external)));

        final List<BookSearchItem> result = searchBooksService.search("silmarillion");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("ext:ol:OL82563W");
    }

    @Test
    void shouldRecordSearchDurationMetrics() {
        when(bookRepository.searchByQuery(eq("hobbit"), any(PageRequest.class)))
                .thenReturn(List.of());
        when(searchProvider.searchAsync(eq("hobbit"), eq(20), any()))
                .thenReturn(CompletableFuture.completedFuture(List.of()));

        searchBooksService.search("hobbit");

        assertThat(meterRegistry.get("catalog.search.total.duration").timer().count()).isEqualTo(1);
        assertThat(meterRegistry.get("catalog.search.local.duration").timer().count()).isEqualTo(1);
        assertThat(meterRegistry.get("catalog.search.external.duration").timer().count()).isEqualTo(1);
    }
}
