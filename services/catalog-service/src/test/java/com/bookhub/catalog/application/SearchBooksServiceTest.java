package com.bookhub.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
                searchBooksService = new SearchBooksService(bookRepository, searchProvider,
                                Duration.ofMillis(100), directExecutor, meterRegistry);
        }

        @Test
        void shouldMergeLocalAndExternalResultsConcurrently() {
                final Book localBook = Book.builder()
                                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                                .title("The Hobbit").authorName("J.R.R. Tolkien")
                                .sourceReference("OL262758W").isbn13("9780261103344").build();

                final BookSearchItem external = new BookSearchItem("OL262758W", "The Hobbit",
                                "J.R.R. Tolkien", "OL262758W", "9780261103344", null, null);

                when(bookRepository.searchByQuery("hobbit", 20))
                                .thenReturn(List.of(localBook));
                when(searchProvider.searchAsync(eq("hobbit"), eq(20), any()))
                                .thenReturn(CompletableFuture.completedFuture(List.of(external)));

                final List<BookSearchItem> result = searchBooksService.search("hobbit", 20, 0);

                assertThat(result).hasSize(1);
                assertThat(result.getFirst().id())
                                .isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        }

        @Test
        void shouldFallbackToLocalResultsWhenExternalTimesOut() {
                final Book localBook = Book.builder()
                                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                                .title("The Hobbit").authorName("J.R.R. Tolkien")
                                .sourceReference("OL262758W").isbn13("9780261103344").build();

                when(bookRepository.searchByQuery("hobbit", 20))
                                .thenReturn(List.of(localBook));
                when(searchProvider.searchAsync(eq("hobbit"), eq(20), any())).thenReturn(
                                CompletableFuture.failedFuture(new RuntimeException("timeout")));

                final List<BookSearchItem> result = searchBooksService.search("hobbit", 20, 0);

                assertThat(result).hasSize(1);
                assertThat(result.getFirst().id())
                                .isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        }

        @Test
        void shouldExposeEphemeralIdForUnpersistedExternalBooks() {
                final BookSearchItem external = new BookSearchItem("OL82563W", "The Silmarillion",
                                "J.R.R. Tolkien", "OL82563W", "9780261102736", null, null);

                when(bookRepository.searchByQuery("silmarillion", 20))
                                .thenReturn(List.of());
                when(searchProvider.searchAsync(eq("silmarillion"), eq(20), any()))
                                .thenReturn(CompletableFuture.completedFuture(List.of(external)));

                final List<BookSearchItem> result = searchBooksService.search("silmarillion", 20, 0);

                assertThat(result).hasSize(1);
                assertThat(result.getFirst().id()).isEqualTo("ext:ol:OL82563W");
        }

        @Test
        void shouldRecordSearchDurationMetrics() {
                when(bookRepository.searchByQuery("hobbit", 20))
                                .thenReturn(List.of());
                when(searchProvider.searchAsync(eq("hobbit"), eq(20), any()))
                                .thenReturn(CompletableFuture.completedFuture(List.of()));

                searchBooksService.search("hobbit", 20, 0);

                assertThat(meterRegistry.get("catalog.search.total.duration").timer().count())
                                .isEqualTo(1);
                assertThat(meterRegistry.get("catalog.search.local.duration").timer().count())
                                .isEqualTo(1);
                assertThat(meterRegistry.get("catalog.search.external.duration").timer().count())
                                .isEqualTo(1);
        }

        @Test
        void shouldUseCandidateLimitAndApplyOffsetLimitSlice() {
                final Book localOne = Book.builder()
                                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174010"))
                                .title("Book One")
                                .authorName("Author")
                                .sourceReference("OL001W")
                                .build();
                final Book localTwo = Book.builder()
                                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174011"))
                                .title("Book Two")
                                .authorName("Author")
                                .sourceReference("OL002W")
                                .build();
                final BookSearchItem externalOne = new BookSearchItem(
                                "ext:ol:OL003W",
                                "Book Three",
                                "Author",
                                "OL003W",
                                null,
                                null,
                                null);
                final BookSearchItem externalTwo = new BookSearchItem(
                                "ext:ol:OL004W",
                                "Book Four",
                                "Author",
                                "OL004W",
                                null,
                                null,
                                null);

                when(bookRepository.searchByQuery("book", 4)).thenReturn(List.of(localOne, localTwo));
                when(searchProvider.searchAsync(eq("book"), eq(4), any()))
                                .thenReturn(CompletableFuture.completedFuture(List.of(externalOne, externalTwo)));

                final List<BookSearchItem> result = searchBooksService.search("book", 2, 2);

                assertThat(result).hasSize(2);
                assertThat(result.getFirst().sourceReference()).isEqualTo("OL003W");
                assertThat(result.get(1).sourceReference()).isEqualTo("OL004W");
                verify(bookRepository).searchByQuery("book", 4);
                verify(searchProvider).searchAsync(eq("book"), eq(4), any());
        }
}
