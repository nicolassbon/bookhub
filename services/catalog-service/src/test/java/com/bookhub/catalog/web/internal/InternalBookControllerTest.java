package com.bookhub.catalog.web.internal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.catalog.application.GetBookForInternalService;
import com.bookhub.catalog.application.error.BookNotFoundException;
import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.web.GlobalExceptionHandler;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InternalBookController.class)
@Import(GlobalExceptionHandler.class)
class InternalBookControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetBookForInternalService getBookForInternalService;

  @Test
  @DisplayName("Should return 200 with book data when book exists")
  void shouldReturn200WithBookDataWhenBookExists() throws Exception {
    final UUID bookId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    final Book book =
        Book.builder()
            .id(bookId)
            .title("Clean Architecture")
            .authorName("Robert C. Martin")
            .sourceReference("OL123W")
            .coverUrl("https://covers.openlibrary.org/b/id/999-L.jpg")
            .pageCount(432)
            .build();

    when(getBookForInternalService.getByIdOrThrow(bookId)).thenReturn(book);

    mockMvc
        .perform(get("/api/v1/internal/books/{bookId}", bookId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookId").value(bookId.toString()))
        .andExpect(jsonPath("$.title").value("Clean Architecture"))
        .andExpect(jsonPath("$.coverUrl").value("https://covers.openlibrary.org/b/id/999-L.jpg"))
        .andExpect(jsonPath("$.pageCount").value(432))
        .andExpect(jsonPath("$.exists").value(true))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  @DisplayName("Should return 200 with null pageCount when book has no page count")
  void shouldReturn200WithNullPageCountWhenBookHasNoPageCount() throws Exception {
    final UUID bookId = UUID.fromString("223e4567-e89b-12d3-a456-426614174000");
    final Book book =
        Book.builder()
            .id(bookId)
            .title("Unknown Pages Book")
            .authorName("Author")
            .sourceReference("OL456W")
            .build();

    when(getBookForInternalService.getByIdOrThrow(bookId)).thenReturn(book);

    mockMvc
        .perform(get("/api/v1/internal/books/{bookId}", bookId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookId").value(bookId.toString()))
        .andExpect(jsonPath("$.pageCount").isEmpty())
        .andExpect(jsonPath("$.exists").value(true));
  }

  @Test
  @DisplayName("Should return 404 when book does not exist")
  void shouldReturn404WhenBookDoesNotExist() throws Exception {
    final UUID bookId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    when(getBookForInternalService.getByIdOrThrow(bookId))
        .thenThrow(new BookNotFoundException("Book not found: " + bookId));

    mockMvc
        .perform(get("/api/v1/internal/books/{bookId}", bookId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"))
        .andExpect(jsonPath("$.path").value("/api/v1/internal/books/" + bookId));
  }

  @Test
  @DisplayName("Should return 400 when bookId is not a valid UUID")
  void shouldReturn400WhenBookIdIsNotAValidUuid() throws Exception {
    mockMvc
        .perform(get("/api/v1/internal/books/{bookId}", "not-a-uuid"))
        .andExpect(status().isBadRequest());
  }
}
