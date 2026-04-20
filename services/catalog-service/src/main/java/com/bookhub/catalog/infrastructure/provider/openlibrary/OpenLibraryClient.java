package com.bookhub.catalog.infrastructure.provider.openlibrary;

import com.bookhub.catalog.application.error.ExternalProviderException;
import com.bookhub.catalog.application.error.ExternalServiceUnavailableException;
import com.bookhub.catalog.application.error.InvalidProviderPayloadException;
import com.bookhub.catalog.application.model.BookSearchItem;
import com.bookhub.catalog.application.support.BookNormalization;
import com.bookhub.catalog.config.OpenLibraryProperties;
import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.domain.SearchProvider;
import com.bookhub.catalog.infrastructure.provider.openlibrary.dto.OpenLibrarySearchResponse;
import com.bookhub.catalog.infrastructure.provider.openlibrary.dto.OpenLibraryWorkResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class OpenLibraryClient implements SearchProvider {

    private final RestClient openLibraryRestClient;
    private final OpenLibraryProperties openLibraryProperties;
    private final MeterRegistry meterRegistry;
    private final CircuitBreaker openLibraryCircuitBreaker;

    public OpenLibraryClient(
            final RestClient openLibraryRestClient,
            final OpenLibraryProperties openLibraryProperties,
            final MeterRegistry meterRegistry,
            final CircuitBreaker openLibraryCircuitBreaker) {
        this.openLibraryRestClient = openLibraryRestClient;
        this.openLibraryProperties = openLibraryProperties;
        this.meterRegistry = meterRegistry;
        this.openLibraryCircuitBreaker = openLibraryCircuitBreaker;
    }

    @Override
    public List<BookSearchItem> search(final String query, final int limit) {
        try {
            final OpenLibrarySearchResponse response = openLibraryCircuitBreaker.executeSupplier(() -> openLibraryRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search.json")
                            .queryParam("q", query)
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                        throw new RestClientException("OpenLibrary search failed");
                    })
                    .body(OpenLibrarySearchResponse.class));

            if (response == null || response.docs() == null) {
                return List.of();
            }

            return response.docs().stream()
                    .map(this::toSearchItem)
                    .toList();
        } catch (CallNotPermittedException exception) {
            meterRegistry.counter("catalog.provider.openlibrary.search.fallbacks", "reason", "circuit_open")
                    .increment();
            log.warn("OpenLibrary search short-circuited by circuit breaker. query='{}'", query);
            return List.of();
        } catch (RestClientException exception) {
            meterRegistry.counter("catalog.provider.openlibrary.search.fallbacks", "reason", "error")
                    .increment();
            return List.of();
        }
    }

    @Override
    public CompletableFuture<List<BookSearchItem>> searchAsync(
            final String query,
            final int limit,
            final Executor executor) {
        return CompletableFuture.supplyAsync(() -> search(query, limit), executor)
                .orTimeout(openLibraryProperties.timeoutMs(), java.util.concurrent.TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> {
                    final boolean timeout = throwable instanceof TimeoutException
                            || (throwable.getCause() != null && throwable.getCause() instanceof TimeoutException);
                    final String reason = timeout ? "timeout" : "error";
                    meterRegistry.counter("catalog.provider.openlibrary.search.fallbacks", "reason", reason)
                            .increment();
                    log.warn("OpenLibrary async search fallback triggered. query='{}', reason='{}'", query, reason);
                    return List.of();
                });
    }

    @Override
    public Book fetchDetail(final String sourceReference) {
        try {
            final String normalized = BookNormalization.normalizeSourceReference(sourceReference);
            final OpenLibraryWorkResponse response = openLibraryCircuitBreaker.executeSupplier(() -> openLibraryRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/works/{workId}.json")
                            .build(normalized))
                    .retrieve()
                    .body(OpenLibraryWorkResponse.class));

            if (response == null) {
                throw new ExternalProviderException("OpenLibrary detail not found", new RestClientException("Empty response"));
            }

            final String title = requireTitle(response);

            return Book.builder()
                    .id(UUID.randomUUID())
                    .title(title)
                    .authorName(resolveAuthor(response.authorNames()))
                    .isbn13(resolveIsbn13(response.isbn13Values()))
                    .sourceReference(normalized)
                    .coverUrl(resolveCoverUrl(response.coverIds()))
                    .publishedYear(resolvePublishedYear(response.firstPublishDate()))
                    .build();
        } catch (CallNotPermittedException exception) {
            throw new ExternalServiceUnavailableException(
                    "OpenLibrary detail is temporarily unavailable",
                    exception,
                    retryAfterSeconds());
        } catch (RestClientException exception) {
            throw new ExternalServiceUnavailableException(
                    "OpenLibrary detail fetch failed",
                    exception,
                    retryAfterSeconds());
        }
    }

    private int retryAfterSeconds() {
        return Math.max(1, (int) (openLibraryProperties.circuitBreaker().waitDurationOpenStateMs() / 1000));
    }

    private BookSearchItem toSearchItem(final OpenLibrarySearchResponse.OpenLibraryBookDoc bookDoc) {
        return new BookSearchItem(
                BookNormalization.normalizeSourceReference(bookDoc.key()),
                bookDoc.title(),
                resolveAuthor(bookDoc.authorNames()),
                BookNormalization.normalizeSourceReference(bookDoc.key()),
                resolveIsbn13(bookDoc.isbn()),
                resolveCoverUrl(bookDoc.coverId()),
                bookDoc.firstPublishYear());
    }

    private String resolveAuthor(final List<String> authorNames) {
        if (authorNames == null || authorNames.isEmpty()) {
            return "Unknown";
        }
        return authorNames.getFirst();
    }

    private String resolveIsbn13(final List<String> isbnValues) {
        if (isbnValues == null || isbnValues.isEmpty()) {
            return null;
        }

        for (String isbnValue : isbnValues) {
            final String normalized = BookNormalization.normalizeIsbn13(isbnValue);
            if (normalized != null && normalized.length() == 13) {
                return normalized;
            }
        }
        return null;
    }

    private String resolveCoverUrl(final Integer coverId) {
        if (coverId == null) {
            return null;
        }
        return "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";
    }

    private String resolveCoverUrl(final List<Integer> coverIds) {
        if (coverIds == null || coverIds.isEmpty()) {
            return null;
        }
        return resolveCoverUrl(coverIds.getFirst());
    }

    private Integer resolvePublishedYear(final String firstPublishDate) {
        if (firstPublishDate == null || firstPublishDate.isBlank()) {
            return null;
        }

        final String trimmed = firstPublishDate.trim();
        if (trimmed.length() < 4) {
            return null;
        }

        final String candidate = trimmed.substring(0, 4);
        if (!candidate.chars().allMatch(Character::isDigit)) {
            return null;
        }

        return Integer.valueOf(candidate);
    }

    private String requireTitle(final OpenLibraryWorkResponse response) {
        final String title = response.title();
        if (title == null || title.isBlank()) {
            throw new InvalidProviderPayloadException("Provider payload missing required title");
        }
        return title;
    }
}
