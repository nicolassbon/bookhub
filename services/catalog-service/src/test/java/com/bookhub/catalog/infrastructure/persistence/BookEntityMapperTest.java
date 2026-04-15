package com.bookhub.catalog.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.bookhub.catalog.domain.Book;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BookEntityMapperTest {

    private final BookEntityMapper mapper = new BookEntityMapper();

    @Test
    void shouldNormalizeIsbnAndSourceReferenceWhenMappingToEntity() {
        final Book book = Book.builder()
                .id(UUID.randomUUID())
                .title("The Hobbit")
                .sourceReference("/works/ol262758w")
                .isbn13("978-0-261-10334-4")
                .build();

        final BookEntity mapped = mapper.toEntity(book);

        assertThat(mapped.getSourceReference()).isEqualTo("OL262758W");
        assertThat(mapped.getIsbn13()).isEqualTo("9780261103344");
    }

    @Test
    void shouldMapBlankValuesAsNullWhenMappingToEntity() {
        final Book book = Book.builder()
                .id(UUID.randomUUID())
                .title("The Hobbit")
                .sourceReference("   ")
                .isbn13("   ")
                .build();

        final BookEntity mapped = mapper.toEntity(book);

        assertThat(mapped.getSourceReference()).isNull();
        assertThat(mapped.getIsbn13()).isNull();
    }
}
