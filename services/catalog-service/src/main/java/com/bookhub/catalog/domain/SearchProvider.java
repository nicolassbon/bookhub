package com.bookhub.catalog.domain;

import com.bookhub.catalog.application.model.BookSearchItem;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface SearchProvider {

  List<BookSearchItem> search(String query, int limit);

  default CompletableFuture<List<BookSearchItem>> searchAsync(
      final String query, final int limit, final Executor executor) {
    return CompletableFuture.supplyAsync(() -> search(query, limit), executor);
  }

  default CompletableFuture<List<BookSearchItem>> searchAsync(final String query, final int limit) {
    return CompletableFuture.completedFuture(search(query, limit));
  }

  Book fetchDetail(String sourceReference);
}
