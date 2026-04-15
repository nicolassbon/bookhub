package com.bookhub.catalog.application;

import com.bookhub.catalog.application.model.BookSearchItem;
import com.bookhub.catalog.application.support.SearchResultMerger;
import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.domain.BookRepository;
import com.bookhub.catalog.domain.SearchProvider;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchBooksService {

    private static final int DEFAULT_LIMIT = 20;
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchBooksService.class);

    private final BookRepository bookRepository;
    private final SearchProvider searchProvider;
    private final Duration providerTimeout;
    private final Executor searchExecutor;
    private final SearchResultMerger searchResultMerger;
    private final MeterRegistry meterRegistry;

    @Autowired
    public SearchBooksService(final BookRepository bookRepository,
            final SearchProvider searchProvider, final Duration providerTimeout,
            final Executor searchExecutor, final SearchResultMerger searchResultMerger,
            final MeterRegistry meterRegistry) {
        this.bookRepository = bookRepository;
        this.searchProvider = searchProvider;
        this.providerTimeout = providerTimeout;
        this.searchExecutor = searchExecutor;
        this.searchResultMerger = searchResultMerger;
        this.meterRegistry = meterRegistry;
    }

    public SearchBooksService(final BookRepository bookRepository,
            final SearchProvider searchProvider, final Duration providerTimeout,
            final Executor searchExecutor, final MeterRegistry meterRegistry) {
        this(bookRepository, searchProvider, providerTimeout, searchExecutor,
                new SearchResultMerger(), meterRegistry);
    }

    public List<BookSearchItem> search(final String query) {
        final long totalStart = System.nanoTime();
        final CompletableFuture<List<Book>> localFuture = CompletableFuture.supplyAsync(() -> {
            final long start = System.nanoTime();
            try {
                return bookRepository.searchByQuery(query,
                        PageRequest.of(0, DEFAULT_LIMIT, Sort.by(Sort.Direction.ASC, "title")));
            } finally {
                meterRegistry.timer("catalog.search.local.duration")
                        .record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            }
        }, searchExecutor);

        final CompletableFuture<List<BookSearchItem>> externalFuture = searchProvider
                .searchAsync(query, DEFAULT_LIMIT, searchExecutor)
                .completeOnTimeout(List.of(), providerTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .exceptionally(ignored -> List.of()).thenApply(results -> {
                    meterRegistry.timer("catalog.search.external.duration")
                            .record(System.nanoTime() - totalStart, TimeUnit.NANOSECONDS);
                    return results;
                });

        final List<Book> localResults = localFuture.join();
        final List<BookSearchItem> externalResults = externalFuture.join();
        final List<BookSearchItem> mergedResults =
                searchResultMerger.merge(localResults, externalResults);
        meterRegistry.timer("catalog.search.total.duration").record(System.nanoTime() - totalStart,
                TimeUnit.NANOSECONDS);
        LOGGER.info(
                "Catalog search completed query='{}' localCount={} externalCount={} mergedCount={}",
                query, localResults.size(), externalResults.size(), mergedResults.size());
        return mergedResults;
    }
}
