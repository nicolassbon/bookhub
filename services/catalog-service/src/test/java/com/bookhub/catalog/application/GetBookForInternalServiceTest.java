package com.bookhub.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.bookhub.catalog.application.error.BookNotFoundException;
import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.domain.BookRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetBookForInternalServiceTest {

  @Mock private BookRepository bookRepository;

  @InjectMocks private GetBookForInternalService getBookForInternalService;

  @Test
  @DisplayName("Should return book when it exists locally")
  void shouldReturnBookWhenItExistsLocally() {
    final UUID bookId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    final Book book =
        Book.builder()
            .id(bookId)
            .title("Clean Architecture")
            .authorName("Robert C. Martin")
            .sourceReference("OL123W")
            .pageCount(432)
            .build();

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

    final Book result = getBookForInternalService.getByIdOrThrow(bookId);

    assertThat(result.getId()).isEqualTo(bookId);
    assertThat(result.getTitle()).isEqualTo("Clean Architecture");
    assertThat(result.getPageCount()).isEqualTo(432);
  }

  @Test
  @DisplayName("Should throw BookNotFoundException when book does not exist")
  void shouldThrowBookNotFoundExceptionWhenBookDoesNotExist() {
    final UUID bookId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> getBookForInternalService.getByIdOrThrow(bookId))
        .isInstanceOf(BookNotFoundException.class)
        .hasMessageContaining(bookId.toString());
  }
}
