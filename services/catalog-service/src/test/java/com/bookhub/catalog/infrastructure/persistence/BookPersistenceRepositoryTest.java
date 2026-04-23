package com.bookhub.catalog.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.domain.BookRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({BookPersistenceRepositoryAdapter.class, BookEntityMapper.class})
class BookPersistenceRepositoryTest {

  @Container
  static final PostgreSQLContainer POSTGRESQL_CONTAINER =
      new PostgreSQLContainer("postgres:16-alpine");

  @DynamicPropertySource
  static void configureDataSource(final DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    registry.add("spring.flyway.enabled", () -> true);
  }

  @Autowired private BookRepository bookRepository;

  @Test
  void shouldPersistAndFindBySourceReference() {
    final Book persisted = bookRepository.save(baseBook("OL123W"));

    final Optional<Book> found = bookRepository.findBySourceReference("OL123W");

    assertThat(found).isPresent();
    assertThat(found.orElseThrow().getId()).isEqualTo(persisted.getId());
  }

  @Test
  void shouldRejectDuplicatedSourceReference() {
    bookRepository.save(baseBook("OL123W"));

    assertThatThrownBy(() -> bookRepository.save(baseBook("OL123W")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void shouldFindByMiddleSubstringInTitleAndAuthor() {
    bookRepository.save(baseBook("OL123W"));
    bookRepository.save(
        Book.builder()
            .id(UUID.randomUUID())
            .title("Clean Architecture")
            .authorName("Robert Martin")
            .isbn13("9780134494166")
            .sourceReference("OL456W")
            .build());

    final var titleMatches = bookRepository.searchByQuery("obbi", 10);
    final var authorMatches = bookRepository.searchByQuery("mart", 10);

    assertThat(titleMatches).extracting(Book::getSourceReference).contains("OL123W");
    assertThat(authorMatches).extracting(Book::getSourceReference).contains("OL456W");
  }

  private Book baseBook(final String sourceReference) {
    return Book.builder()
        .id(UUID.randomUUID())
        .title("The Hobbit")
        .authorName("J.R.R. Tolkien")
        .isbn13("9780261103344")
        .sourceReference(sourceReference)
        .coverUrl("https://covers.openlibrary.org/b/id/123-L.jpg")
        .publishedYear(1937)
        .build();
  }
}
