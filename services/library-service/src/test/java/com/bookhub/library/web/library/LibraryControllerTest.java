package com.bookhub.library.web.library;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.library.application.AddBookToLibraryService;
import com.bookhub.library.application.GetUserLibraryService;
import com.bookhub.library.application.UpdateReadingProgressService;
import com.bookhub.library.application.UpdateReadingStateService;
import com.bookhub.library.application.error.BookNotFoundInCatalogException;
import com.bookhub.library.application.error.CatalogIntegrationException;
import com.bookhub.library.application.error.DuplicateLibraryEntryException;
import com.bookhub.library.application.error.LibraryEntryNotFoundException;
import com.bookhub.library.application.error.LibraryEntryOwnershipException;
import com.bookhub.library.config.SecurityConfig;
import com.bookhub.library.domain.BookSnapshot;
import com.bookhub.library.domain.ReadingProgressException;
import com.bookhub.library.domain.ReadingState;
import com.bookhub.library.domain.UserBook;
import com.bookhub.library.web.GlobalExceptionHandler;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LibraryController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class LibraryControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AddBookToLibraryService addBookToLibraryService;
  @MockitoBean private GetUserLibraryService getUserLibraryService;
  @MockitoBean private UpdateReadingStateService updateReadingStateService;
  @MockitoBean private UpdateReadingProgressService updateReadingProgressService;
  @MockitoBean private JwtDecoder jwtDecoder;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID ENTRY_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

  private JwtRequestPostProcessor authenticatedJwt() {
    return jwt().jwt(builder -> builder.subject(USER_ID.toString()));
  }

  @Nested
  @DisplayName("POST /api/v1/library/books")
  class AddBook {

    @Test
    @DisplayName("Should return 201 when book is added successfully")
    void shouldReturn201WhenBookAdded() throws Exception {
      final UserBook userBook =
          UserBook.create(
              USER_ID,
              BOOK_ID,
              ReadingState.WANT_TO_READ,
              new BookSnapshot("Clean Code", null, 464));
      userBook.setId(ENTRY_ID);

      when(addBookToLibraryService.execute(eq(USER_ID), eq(BOOK_ID), any())).thenReturn(userBook);

      mockMvc
          .perform(
              post("/api/v1/library/books")
                  .with(authenticatedJwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"bookId": "00000000-0000-0000-0000-000000000002", "initialState": "WANT_TO_READ"}
                      """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.entryId").value(ENTRY_ID.toString()))
          .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
          .andExpect(jsonPath("$.bookId").value(BOOK_ID.toString()))
          .andExpect(jsonPath("$.book.title").value("Clean Code"))
          .andExpect(jsonPath("$.state").value("WANT_TO_READ"))
          .andExpect(jsonPath("$.progress.pagesRead").value(0))
          .andExpect(jsonPath("$.progress.percentage").value(0));
    }

    @Test
    @DisplayName("Should return 400 when bookId is null")
    void shouldReturn400WhenBookIdIsNull() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/library/books")
                  .with(authenticatedJwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"initialState": "WANT_TO_READ"}
                      """))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when book not found in catalog")
    void shouldReturn400WhenBookNotInCatalog() throws Exception {
      when(addBookToLibraryService.execute(eq(USER_ID), eq(BOOK_ID), any()))
          .thenThrow(new BookNotFoundInCatalogException("Book not found in catalog"));

      mockMvc
          .perform(
              post("/api/v1/library/books")
                  .with(authenticatedJwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"bookId": "00000000-0000-0000-0000-000000000002"}
                      """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND_IN_CATALOG"));
    }

    @Test
    @DisplayName("Should return 502 when catalog integration fails")
    void shouldReturn502WhenCatalogIntegrationFails() throws Exception {
      when(addBookToLibraryService.execute(eq(USER_ID), eq(BOOK_ID), any()))
          .thenThrow(new CatalogIntegrationException("Catalog timeout"));

      mockMvc
          .perform(
              post("/api/v1/library/books")
                  .with(authenticatedJwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"bookId": "00000000-0000-0000-0000-000000000002"}
                      """))
          .andExpect(status().isBadGateway())
          .andExpect(jsonPath("$.code").value("CATALOG_INTEGRATION_ERROR"));
    }

    @Test
    @DisplayName("Should return 409 when book is already in library")
    void shouldReturn409WhenDuplicate() throws Exception {
      when(addBookToLibraryService.execute(eq(USER_ID), eq(BOOK_ID), any()))
          .thenThrow(new DuplicateLibraryEntryException("Already in library"));

      mockMvc
          .perform(
              post("/api/v1/library/books")
                  .with(authenticatedJwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"bookId": "00000000-0000-0000-0000-000000000002"}
                      """))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.code").value("DUPLICATE_LIBRARY_ENTRY"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/library/books")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"bookId": "00000000-0000-0000-0000-000000000002"}
                      """))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/library/me")
  class LibrarySummary {

    @Test
    @DisplayName("Should return library summary")
    void shouldReturnLibrarySummary() throws Exception {
      when(getUserLibraryService.getLibrarySummary(USER_ID))
          .thenReturn(new GetUserLibraryService.LibrarySummary(5, 2, 2, 1));

      mockMvc
          .perform(get("/api/v1/library/me").with(authenticatedJwt()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.total").value(5))
          .andExpect(jsonPath("$.wantToRead").value(2))
          .andExpect(jsonPath("$.reading").value(2))
          .andExpect(jsonPath("$.read").value(1));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/library/me/books")
  class LibraryBooks {

    @Test
    @DisplayName("Should return all library entries without filter")
    void shouldReturnAllEntries() throws Exception {
      final UserBook entry =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.READING, new BookSnapshot("Clean Code", null, 464));
      entry.setId(ENTRY_ID);

      when(getUserLibraryService.getLibraryEntries(USER_ID)).thenReturn(List.of(entry));

      mockMvc
          .perform(get("/api/v1/library/me/books").with(authenticatedJwt()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].entryId").value(ENTRY_ID.toString()))
          .andExpect(jsonPath("$[0].state").value("READING"));
    }

    @Test
    @DisplayName("Should return filtered entries by state")
    void shouldReturnFilteredEntries() throws Exception {
      final UserBook entry =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.READ, new BookSnapshot("Clean Code", null, 464));
      entry.setId(ENTRY_ID);

      when(getUserLibraryService.getLibraryEntriesByState(USER_ID, ReadingState.READ))
          .thenReturn(List.of(entry));

      mockMvc
          .perform(get("/api/v1/library/me/books").param("state", "READ").with(authenticatedJwt()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].state").value("READ"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/library/books/{entryId}")
  class LibraryEntryById {

    @Test
    @DisplayName("Should return the owned library entry")
    void shouldReturnOwnedEntry() throws Exception {
      final UserBook entry =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.READING, new BookSnapshot("Clean Code", null, 464));
      entry.setId(ENTRY_ID);

      when(getUserLibraryService.getLibraryEntry(USER_ID, ENTRY_ID)).thenReturn(entry);

      mockMvc
          .perform(get("/api/v1/library/books/{entryId}", ENTRY_ID).with(authenticatedJwt()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.entryId").value(ENTRY_ID.toString()))
          .andExpect(jsonPath("$.book.title").value("Clean Code"))
          .andExpect(jsonPath("$.progress.percentage").value(0));
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/library/books/{entryId}/state")
  class UpdateState {

    @Test
    @DisplayName("Should return 200 when state is updated")
    void shouldReturn200WhenStateUpdated() throws Exception {
      final UserBook updated =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.READING, new BookSnapshot("Clean Code", null, 464));
      updated.setId(ENTRY_ID);

      when(updateReadingStateService.execute(USER_ID, ENTRY_ID, ReadingState.READING))
          .thenReturn(updated);

      mockMvc
          .perform(
              patch("/api/v1/library/books/{entryId}/state", ENTRY_ID)
                  .with(authenticatedJwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"state": "READING"}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.state").value("READING"));
    }

    @Test
    @DisplayName("Should return 404 when entry not found")
    void shouldReturn404WhenEntryNotFound() throws Exception {
      when(updateReadingStateService.execute(eq(USER_ID), eq(ENTRY_ID), any()))
          .thenThrow(new LibraryEntryNotFoundException("Not found"));

      mockMvc
          .perform(
              patch("/api/v1/library/books/{entryId}/state", ENTRY_ID)
                  .with(authenticatedJwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"state": "READING"}
                      """))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("LIBRARY_ENTRY_NOT_FOUND"));
    }

    @Test
    @DisplayName("Should return 403 when user does not own entry")
    void shouldReturn403WhenNotOwner() throws Exception {
      when(updateReadingStateService.execute(eq(USER_ID), eq(ENTRY_ID), any()))
          .thenThrow(new LibraryEntryOwnershipException("Not owner"));

      mockMvc
          .perform(
              patch("/api/v1/library/books/{entryId}/state", ENTRY_ID)
                  .with(authenticatedJwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"state": "READING"}
                      """))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("LIBRARY_ENTRY_OWNERSHIP"));
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/library/books/{entryId}/progress")
  class UpdateProgress {

    @Test
    void shouldReturn200WhenProgressUpdated() throws Exception {
      final UserBook updated =
          UserBook.create(
              USER_ID, BOOK_ID, ReadingState.READING, new BookSnapshot("Clean Code", null, 400));
      updated.setId(ENTRY_ID);
      updated.updateProgress(100);

      when(updateReadingProgressService.execute(USER_ID, ENTRY_ID, 100)).thenReturn(updated);

      mockMvc
          .perform(
              patch("/api/v1/library/books/{entryId}/progress", ENTRY_ID)
                  .with(authenticatedJwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"pagesRead": 100}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.entryId").value(ENTRY_ID.toString()))
          .andExpect(jsonPath("$.pagesRead").value(100))
          .andExpect(jsonPath("$.completionPercentage").value(25))
          .andExpect(jsonPath("$.readingState").value("READING"));
    }

    @Test
    void shouldReturnNullPercentageWhenPageCountIsUnknown() throws Exception {
      final UserBook updated =
          UserBook.create(
              USER_ID,
              BOOK_ID,
              ReadingState.WANT_TO_READ,
              new BookSnapshot("Clean Code", null, null));
      updated.setId(ENTRY_ID);
      updated.updateProgress(100);

      when(updateReadingProgressService.execute(USER_ID, ENTRY_ID, 100)).thenReturn(updated);

      mockMvc
          .perform(
              patch("/api/v1/library/books/{entryId}/progress", ENTRY_ID)
                  .with(authenticatedJwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"pagesRead": 100}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.entryId").value(ENTRY_ID.toString()))
          .andExpect(jsonPath("$.pagesRead").value(100))
          .andExpect(jsonPath("$.completionPercentage").value((Object) null))
          .andExpect(jsonPath("$.readingState").value("READING"));
    }

    @Test
    void shouldReturn400WhenPagesReadIsMissing() throws Exception {
      mockMvc
          .perform(
              patch("/api/v1/library/books/{entryId}/progress", ENTRY_ID)
                  .with(authenticatedJwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturn403WhenNotOwner() throws Exception {
      when(updateReadingProgressService.execute(eq(USER_ID), eq(ENTRY_ID), eq(100)))
          .thenThrow(new LibraryEntryOwnershipException("Not owner"));

      mockMvc
          .perform(
              patch("/api/v1/library/books/{entryId}/progress", ENTRY_ID)
                  .with(authenticatedJwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"pagesRead": 100}
                      """))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("LIBRARY_ENTRY_OWNERSHIP"));
    }

    @Test
    void shouldReturn400WhenReadingProgressIsInvalid() throws Exception {
      when(updateReadingProgressService.execute(eq(USER_ID), eq(ENTRY_ID), eq(100)))
          .thenThrow(new ReadingProgressException("Pages read cannot exceed total pages"));

      mockMvc
          .perform(
              patch("/api/v1/library/books/{entryId}/progress", ENTRY_ID)
                  .with(authenticatedJwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"pagesRead": 100}
                      """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("INVALID_READING_PROGRESS"));
    }

    @Test
    void shouldReturn400WhenDomainValidationThrowsIllegalArgument() throws Exception {
      when(updateReadingProgressService.execute(eq(USER_ID), eq(ENTRY_ID), eq(100)))
          .thenThrow(new IllegalArgumentException("rating must be between 1 and 5"));

      mockMvc
          .perform(
              patch("/api/v1/library/books/{entryId}/progress", ENTRY_ID)
                  .with(authenticatedJwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"pagesRead": 100}
                      """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
          .andExpect(jsonPath("$.message").value("rating must be between 1 and 5"));
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      mockMvc
          .perform(
              patch("/api/v1/library/books/{entryId}/progress", ENTRY_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"pagesRead": 100}
                      """))
          .andExpect(status().isUnauthorized());
    }
  }
}
