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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SearchBooksService {

  private static final int DEFAULT_LIMIT = 20;
  private static final int MAX_LIMIT = 100;

  private final BookRepository bookRepository;
  private final SearchProvider searchProvider;
  private final Duration providerTimeout;
  private final Executor searchExecutor;
  private final SearchResultMerger searchResultMerger;
  private final MeterRegistry meterRegistry;

  @Autowired
  public SearchBooksService(
      final BookRepository bookRepository,
      final SearchProvider searchProvider,
      final Duration providerTimeout,
      final Executor searchExecutor,
      final SearchResultMerger searchResultMerger,
      final MeterRegistry meterRegistry) {
    this.bookRepository = bookRepository;
    this.searchProvider = searchProvider;
    this.providerTimeout = providerTimeout;
    this.searchExecutor = searchExecutor;
    this.searchResultMerger = searchResultMerger;
    this.meterRegistry = meterRegistry;
  }

  public SearchBooksService(
      final BookRepository bookRepository,
      final SearchProvider searchProvider,
      final Duration providerTimeout,
      final Executor searchExecutor,
      final MeterRegistry meterRegistry) {
    this(
        bookRepository,
        searchProvider,
        providerTimeout,
        searchExecutor,
        new SearchResultMerger(),
        meterRegistry);
  }

  public List<BookSearchItem> search(final String query, final int limit, final int offset) {
    final int normalizedLimit = normalizeLimit(limit);
    final int normalizedOffset = normalizeOffset(offset);
    final int candidateLimit = normalizedLimit + normalizedOffset;

    final long totalStart = System.nanoTime();
    final CompletableFuture<List<Book>> localFuture =
        CompletableFuture.supplyAsync(
            () -> {
              final long start = System.nanoTime();
              try {
                return bookRepository.searchByQuery(query, candidateLimit);
              } finally {
                meterRegistry
                    .timer("catalog.search.local.duration")
                    .record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
              }
            },
            searchExecutor);

    final CompletableFuture<List<BookSearchItem>> externalFuture =
        searchProvider
            .searchAsync(query, candidateLimit, searchExecutor)
            .completeOnTimeout(List.of(), providerTimeout.toMillis(), TimeUnit.MILLISECONDS)
            .exceptionally(ignored -> List.of())
            .thenApply(
                results -> {
                  meterRegistry
                      .timer("catalog.search.external.duration")
                      .record(System.nanoTime() - totalStart, TimeUnit.NANOSECONDS);
                  return results;
                });

    final List<Book> localResults = localFuture.join();
    final List<BookSearchItem> externalResults = externalFuture.join();
    final List<BookSearchItem> mergedResults =
        searchResultMerger.merge(localResults, externalResults);
    final List<BookSearchItem> pagedResults =
        mergedResults.stream().skip(normalizedOffset).limit(normalizedLimit).toList();

    meterRegistry
        .timer("catalog.search.total.duration")
        .record(System.nanoTime() - totalStart, TimeUnit.NANOSECONDS);
    log.info(
        "Catalog search completed query='{}' localCount={} externalCount={} mergedCount={} pageCount={} candidateLimit={}",
        query,
        localResults.size(),
        externalResults.size(),
        mergedResults.size(),
        pagedResults.size(),
        candidateLimit);
    return pagedResults;
  }

  private int normalizeLimit(final int limit) {
    if (limit <= 0) {
      return DEFAULT_LIMIT;
    }
    return Math.min(limit, MAX_LIMIT);
  }

  private int normalizeOffset(final int offset) {
    return Math.max(offset, 0);
  }
}
