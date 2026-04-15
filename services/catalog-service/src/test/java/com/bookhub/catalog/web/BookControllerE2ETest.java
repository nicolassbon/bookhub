package com.bookhub.catalog.web;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.domain.BookRepository;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookControllerE2ETest {

        @Container
        static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
                        new PostgreSQLContainer<>("postgres:16-alpine");

        static final WireMockServer WIRE_MOCK_SERVER =
                        new WireMockServer(WireMockConfiguration.options().dynamicPort());

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private BookRepository bookRepository;

        @DynamicPropertySource
        static void configureProperties(final DynamicPropertyRegistry registry) {
                registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
                registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
                registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
                registry.add("spring.flyway.enabled", () -> true);
                registry.add("catalog.providers.openlibrary.url", WIRE_MOCK_SERVER::baseUrl);
        }

        @BeforeAll
        static void startWireMock() {
                WIRE_MOCK_SERVER.start();
        }

        @AfterAll
        static void stopWireMock() {
                WIRE_MOCK_SERVER.stop();
        }

        @Test
        void shouldMergeSearchResultsAndPersistExternalBookOnDetailFetch() throws Exception {
                final Book localBook = bookRepository.save(Book.builder()
                                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                                .title("The Hobbit").authorName("J.R.R. Tolkien")
                                .sourceReference("OL123W").isbn13("9780261103344").build());

                WIRE_MOCK_SERVER.stubFor(
                                WireMock.get(urlPathEqualTo("/search.json")).willReturn(okJson("""
                                                {
                                                  "docs": [
                                                    {
                                                      "key": "/works/OL123W",
                                                      "title": "The Hobbit",
                                                      "author_name": ["J.R.R. Tolkien"],
                                                      "isbn": ["9780261103344"],
                                                      "cover_i": 111,
                                                      "first_publish_year": 1937
                                                    },
                                                    {
                                                      "key": "/works/OL999W",
                                                      "title": "Unfinished Tales",
                                                      "author_name": ["J.R.R. Tolkien"],
                                                      "isbn": ["9780261102163"],
                                                      "cover_i": 222,
                                                      "first_publish_year": 1980
                                                    }
                                                  ]
                                                }
                                                """)));

                mockMvc.perform(get("/api/v1/books").queryParam("q", "tolkien"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(localBook.getId().toString()))
                                .andExpect(jsonPath("$[1].id").value("ext:ol:OL999W"));

                WIRE_MOCK_SERVER.stubFor(WireMock.get(urlPathMatching("/works/OL999W.json"))
                                .willReturn(okJson("""
                                                {
                                                  "key": "/works/OL999W",
                                                  "title": "Unfinished Tales",
                                                  "covers": [222]
                                                }
                                                """)));

                mockMvc.perform(get("/api/v1/books/{id}", "ext:ol:OL999W"))
                                .andExpect(status().isOk()).andExpect(jsonPath("$.id").isNotEmpty())
                                .andExpect(jsonPath("$.sourceReference").value("OL999W"))
                                .andExpect(jsonPath("$.title").value("Unfinished Tales"));

                assertThat(bookRepository.findBySourceReference("OL999W")).isPresent();
        }
}
