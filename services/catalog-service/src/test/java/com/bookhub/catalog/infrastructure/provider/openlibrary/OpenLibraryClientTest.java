package com.bookhub.catalog.infrastructure.provider.openlibrary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.bookhub.catalog.application.error.ExternalProviderException;
import com.bookhub.catalog.application.model.BookSearchItem;
import com.bookhub.catalog.config.OpenLibraryProperties;
import com.bookhub.catalog.domain.Book;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OpenLibraryClientTest {

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;
    private OpenLibraryClient openLibraryClient;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder().baseUrl("https://openlibrary.org");
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        meterRegistry = new SimpleMeterRegistry();
        openLibraryClient = new OpenLibraryClient(
                restClientBuilder.build(),
                new OpenLibraryProperties(true, "https://openlibrary.org", 50),
                meterRegistry);
    }

    @Test
    void shouldParseSearchResponse() {
        mockServer.expect(requestTo("https://openlibrary.org/search.json?q=hobbit&limit=20"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "docs": [
                            {
                              "key": "/works/ol262758w",
                              "title": "The Hobbit",
                              "author_name": ["J.R.R. Tolkien"],
                              "isbn": ["9780261103344"],
                              "cover_i": 123,
                              "first_publish_year": 1937
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        final List<BookSearchItem> result = openLibraryClient.search("hobbit", 20);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().sourceReference()).isEqualTo("OL262758W");
        assertThat(result.getFirst().isbn13()).isEqualTo("9780261103344");
    }

    @Test
    void shouldReturnEmptyWhenSearchTimesOut() {
        mockServer.expect(requestTo("https://openlibrary.org/search.json?q=hobbit&limit=20"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                    }
                    return withSuccess("""
                            {
                              "docs": []
                            }
                            """, MediaType.APPLICATION_JSON).createResponse(request);
                });

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            final List<BookSearchItem> result = openLibraryClient.searchAsync("hobbit", 20, executorService).join();

            assertThat(result).isEmpty();
            assertThat(meterRegistry.get("catalog.provider.openlibrary.search.fallbacks")
                    .tag("reason", "timeout")
                    .counter()
                    .count()).isEqualTo(1.0);
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void shouldWrapDetailFailuresAsExternalProviderException() {
        mockServer.expect(requestTo("https://openlibrary.org/works/OL404W.json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThatThrownBy(() -> openLibraryClient.fetchDetail("OL404W"))
                .isInstanceOf(ExternalProviderException.class);
    }

    @Test
    void shouldMapDetailResponse() {
        mockServer.expect(requestTo("https://openlibrary.org/works/OL123W.json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "key": "/works/OL123W",
                          "title": "The Hobbit",
                          "author_name": ["J.R.R. Tolkien"],
                          "isbn_13": ["9780261103344"],
                          "first_publish_date": "1937-09-21",
                          "covers": [123]
                        }
                        """, MediaType.APPLICATION_JSON));

        final Book detail = openLibraryClient.fetchDetail("OL123W");

        assertThat(detail.getTitle()).isEqualTo("The Hobbit");
        assertThat(detail.getSourceReference()).isEqualTo("OL123W");
        assertThat(detail.getAuthorName()).isEqualTo("J.R.R. Tolkien");
        assertThat(detail.getIsbn13()).isEqualTo("9780261103344");
        assertThat(detail.getPublishedYear()).isEqualTo(1937);
        assertThat(detail.getCoverUrl()).isEqualTo("https://covers.openlibrary.org/b/id/123-L.jpg");
    }

    @Test
    void shouldMapMalformedDetailWithSafeDefaults() {
        mockServer.expect(requestTo("https://openlibrary.org/works/OLMALFORMEDW.json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "key": "/works/OLMALFORMEDW",
                          "title": "Unknown Work",
                          "covers": []
                        }
                        """, MediaType.APPLICATION_JSON));

        final Book detail = openLibraryClient.fetchDetail("OLMALFORMEDW");

        assertThat(detail.getTitle()).isEqualTo("Unknown Work");
        assertThat(detail.getAuthorName()).isEqualTo("Unknown");
        assertThat(detail.getIsbn13()).isNull();
        assertThat(detail.getPublishedYear()).isNull();
        assertThat(detail.getCoverUrl()).isNull();
    }
}
