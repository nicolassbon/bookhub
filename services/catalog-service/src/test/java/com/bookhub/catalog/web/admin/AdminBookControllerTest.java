package com.bookhub.catalog.web.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.catalog.application.admin.AdminImportDegradedException;
import com.bookhub.catalog.application.admin.ImportBookService;
import com.bookhub.catalog.application.admin.ListBooksAdminService;
import com.bookhub.catalog.application.admin.UpdateBookService;
import com.bookhub.catalog.application.error.BookNotFoundException;
import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.web.GlobalExceptionHandler;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminBookController.class)
@AutoConfigureMockMvc(addFilters = false) // Security is tested in SecurityIntegrationTest
@Import(GlobalExceptionHandler.class)
class AdminBookControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ListBooksAdminService listBooksAdminService;
  @MockitoBean private UpdateBookService updateBookService;
  @MockitoBean private ImportBookService importBookService;

  @Test
  void shouldReturnPaginatedBookList() throws Exception {
    final Book book =
        Book.builder()
            .id(UUID.randomUUID())
            .title("The Hobbit")
            .authorName("J.R.R. Tolkien")
            .isbn13("9780261103344")
            .sourceReference("OL262758W")
            .coverUrl("https://cover.com/hobbit.jpg")
            .publishedYear(1937)
            .pageCount(310)
            .createdAt(Instant.parse("2026-05-09T10:00:00Z"))
            .build();

    final ListBooksAdminService.PagedBooksResult result =
        new ListBooksAdminService.PagedBooksResult(List.of(book), 0, 20, 1, 1);

    when(listBooksAdminService.list(0, 20)).thenReturn(result);

    mockMvc
        .perform(get("/api/v1/admin/books").queryParam("page", "0").queryParam("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.items[0].id").value(book.getId().toString()))
        .andExpect(jsonPath("$.items[0].title").value("The Hobbit"))
        .andExpect(jsonPath("$.items[0].createdAt").value("2026-05-09T10:00:00Z"));
  }

  @Test
  void shouldUpdateBook() throws Exception {
    final UUID bookId = UUID.randomUUID();
    final Book updated =
        Book.builder()
            .id(bookId)
            .title("Corrected Title")
            .authorName("Corrected Author")
            .isbn13("1234567890123")
            .sourceReference("OL123W")
            .publishedYear(2020)
            .pageCount(400)
            .build();

    when(updateBookService.update(eq(bookId), any(UpdateBookService.UpdateBookCommand.class)))
        .thenReturn(updated);

    mockMvc
        .perform(
            patch("/api/v1/admin/books/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "title": "Corrected Title",
                      "authorName": "Corrected Author"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(bookId.toString()))
        .andExpect(jsonPath("$.title").value("Corrected Title"))
        .andExpect(jsonPath("$.authorName").value("Corrected Author"));
  }

  @Test
  void shouldReturnNotFoundWhenUpdatingNonExistentBook() throws Exception {
    final UUID bookId = UUID.randomUUID();

    when(updateBookService.update(eq(bookId), any(UpdateBookService.UpdateBookCommand.class)))
        .thenThrow(new BookNotFoundException("Book not found"));

    mockMvc
        .perform(
            patch("/api/v1/admin/books/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title": "X"}
                    """))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
  }

  @Test
  void shouldImportBookSuccessfully() throws Exception {
    final Book imported =
        Book.builder()
            .id(UUID.randomUUID())
            .title("Imported Book")
            .authorName("Author")
            .sourceReference("OL999W")
            .source("OPEN_LIBRARY")
            .build();

    when(importBookService.importBook(any(ImportBookService.ImportBookCommand.class)))
        .thenReturn(new ImportBookService.ImportBookResult(imported, false));

    mockMvc
        .perform(
            post("/api/v1/admin/books/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "sourceReference": "OL999W",
                      "provider": "OPEN_LIBRARY"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Imported Book"))
        .andExpect(jsonPath("$.sourceReference").value("OL999W"));
  }

  @Test
  void shouldReturnValidationErrorWhenImportingWithMissingFields() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/admin/books/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "sourceReference": "OL999W"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  void shouldReturnServiceUnavailableWhenImportProviderIsDegraded() throws Exception {
    when(importBookService.importBook(any(ImportBookService.ImportBookCommand.class)))
        .thenThrow(new AdminImportDegradedException("Provider degraded"));

    mockMvc
        .perform(
            post("/api/v1/admin/books/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "sourceReference": "OL999W",
                      "provider": "OPEN_LIBRARY"
                    }
                    """))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.code").value("EXTERNAL_SERVICE_UNAVAILABLE"));
  }

  @Test
  void shouldFilterBooksBySourceWhenSourceParamProvided() throws Exception {
    final Book book =
        Book.builder()
            .id(UUID.randomUUID())
            .title("Filtered Book")
            .authorName("Author One")
            .sourceReference("OL123W")
            .source("OPEN_LIBRARY")
            .createdAt(Instant.parse("2026-05-09T10:00:00Z"))
            .build();

    final ListBooksAdminService.PagedBooksResult result =
        new ListBooksAdminService.PagedBooksResult(List.of(book), 0, 20, 1, 1);

    when(listBooksAdminService.list(0, 20, "OPEN_LIBRARY")).thenReturn(result);

    mockMvc
        .perform(
            get("/api/v1/admin/books")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .queryParam("source", "OPEN_LIBRARY"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].title").value("Filtered Book"))
        .andExpect(jsonPath("$.items[0].source").value("OPEN_LIBRARY"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  void shouldReturn201WhenImportingNewBook() throws Exception {
    final Book imported =
        Book.builder()
            .id(UUID.randomUUID())
            .title("Newly Imported")
            .authorName("Author")
            .sourceReference("OL999W")
            .source("OPEN_LIBRARY")
            .build();

    when(importBookService.importBook(any(ImportBookService.ImportBookCommand.class)))
        .thenReturn(new ImportBookService.ImportBookResult(imported, false));

    mockMvc
        .perform(
            post("/api/v1/admin/books/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "sourceReference": "OL999W",
                      "provider": "OPEN_LIBRARY"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Newly Imported"))
        .andExpect(jsonPath("$.sourceReference").value("OL999W"));
  }

  @Test
  void shouldReturn200WhenImportingExistingBook() throws Exception {
    final Book existing =
        Book.builder()
            .id(UUID.randomUUID())
            .title("Already Exists")
            .authorName("Author")
            .sourceReference("OL999W")
            .source("OPEN_LIBRARY")
            .createdAt(Instant.parse("2026-05-01T10:00:00Z"))
            .build();

    when(importBookService.importBook(any(ImportBookService.ImportBookCommand.class)))
        .thenReturn(new ImportBookService.ImportBookResult(existing, true));

    mockMvc
        .perform(
            post("/api/v1/admin/books/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "sourceReference": "OL999W",
                      "provider": "OPEN_LIBRARY"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Already Exists"))
        .andExpect(jsonPath("$.sourceReference").value("OL999W"));
  }

  @Test
  void shouldReturnEmptyListWhenFilteringByNonMatchingSource() throws Exception {
    final ListBooksAdminService.PagedBooksResult emptyResult =
        new ListBooksAdminService.PagedBooksResult(List.of(), 0, 20, 0, 0);

    when(listBooksAdminService.list(0, 20, "GOOGLE_BOOKS")).thenReturn(emptyResult);

    mockMvc
        .perform(
            get("/api/v1/admin/books")
                .queryParam("source", "GOOGLE_BOOKS"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items").isEmpty())
        .andExpect(jsonPath("$.totalElements").value(0))
        .andExpect(jsonPath("$.totalPages").value(0));
  }

  @Test
  void shouldFallBackToUnfilteredListWhenSourceParamIsBlank() throws Exception {
    final Book book =
        Book.builder()
            .id(UUID.randomUUID())
            .title("Any Book")
            .authorName("Author")
            .sourceReference("OL123W")
            .source("OPEN_LIBRARY")
            .createdAt(Instant.parse("2026-05-09T10:00:00Z"))
            .build();

    final ListBooksAdminService.PagedBooksResult result =
        new ListBooksAdminService.PagedBooksResult(List.of(book), 0, 20, 1, 1);

    when(listBooksAdminService.list(0, 20)).thenReturn(result);

    mockMvc
        .perform(
            get("/api/v1/admin/books").queryParam("source", ""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].title").value("Any Book"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }
}
