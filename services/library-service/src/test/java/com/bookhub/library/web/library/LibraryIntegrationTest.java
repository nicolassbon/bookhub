package com.bookhub.library.web.library;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.library.application.error.CatalogIntegrationException;
import com.bookhub.library.infrastructure.client.CatalogBook;
import com.bookhub.library.infrastructure.client.CatalogServiceClient;
import com.bookhub.library.infrastructure.persistence.JpaUserBookRepository;
import com.bookhub.library.infrastructure.persistence.UserBookEntity;
import com.bookhub.library.support.PostgreSqlIntegrationTest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
class LibraryIntegrationTest extends PostgreSqlIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JpaUserBookRepository jpaUserBookRepository;

  @MockitoBean private CatalogServiceClient catalogServiceClient;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID UNKNOWN_PAGE_COUNT_BOOK_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000099");

  private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor authenticatedJwt() {
    return SecurityMockMvcRequestPostProcessors.jwt()
        .jwt(builder -> builder.subject(USER_ID.toString()));
  }

  @Test
  @DisplayName("Full lifecycle: add book → get summary → list → update state")
  void fullReadingLifecycle() throws Exception {
    when(catalogServiceClient.findBookById(BOOK_ID))
        .thenReturn(Optional.of(new CatalogBook(BOOK_ID, "Clean Code", null, 464)));

    final MvcResult addResult =
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
            .andExpect(jsonPath("$.bookId").value(BOOK_ID.toString()))
            .andExpect(jsonPath("$.book.title").value("Clean Code"))
            .andExpect(jsonPath("$.book.pageCount").value(464))
            .andExpect(jsonPath("$.state").value("WANT_TO_READ"))
            .andReturn();

    final String entryIdStr =
        com.jayway.jsonpath.JsonPath.read(
            addResult.getResponse().getContentAsString(), "$.entryId");
    final UUID entryId = UUID.fromString(entryIdStr);

    final Optional<UserBookEntity> persisted = jpaUserBookRepository.findById(entryId);
    assertThat(persisted).isPresent();
    assertThat(persisted.get().getBookId()).isEqualTo(BOOK_ID);
    assertThat(persisted.get().getBookTitle()).isEqualTo("Clean Code");
    assertThat(persisted.get().getBookPageCount()).isEqualTo(464);

    mockMvc
        .perform(get("/api/v1/library/me").with(authenticatedJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(1))
        .andExpect(jsonPath("$.wantToRead").value(1))
        .andExpect(jsonPath("$.reading").value(0));

    mockMvc
        .perform(get("/api/v1/library/me/books").with(authenticatedJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].entryId").value(entryId.toString()))
        .andExpect(jsonPath("$[0].book.title").value("Clean Code"));

    mockMvc
        .perform(get("/api/v1/library/books/{entryId}", entryId).with(authenticatedJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.entryId").value(entryId.toString()))
        .andExpect(jsonPath("$.book.title").value("Clean Code"))
        .andExpect(jsonPath("$.book.pageCount").value(464));

    mockMvc
        .perform(
            get("/api/v1/library/me/books").param("state", "WANT_TO_READ").with(authenticatedJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));

    mockMvc
        .perform(
            patch("/api/v1/library/books/{entryId}/state", entryId)
                .with(authenticatedJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"state": "READING"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.state").value("READING"));

    mockMvc
        .perform(get("/api/v1/library/me").with(authenticatedJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.wantToRead").value(0))
        .andExpect(jsonPath("$.reading").value(1));

    mockMvc
        .perform(
            post("/api/v1/library/books")
                .with(authenticatedJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"bookId": "00000000-0000-0000-0000-000000000002"}
                    """))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("Should return 400 when book does not exist in catalog")
  void shouldReturn400WhenBookNotInCatalog() throws Exception {
    final UUID unknownBook = UUID.fromString("99999999-9999-9999-9999-999999999999");
    when(catalogServiceClient.findBookById(unknownBook)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/v1/library/books")
                .with(authenticatedJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"bookId": "99999999-9999-9999-9999-999999999999"}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND_IN_CATALOG"));
  }

  @Test
  @DisplayName("Should return 502 when catalog call fails technically")
  void shouldReturn502WhenCatalogFailsTechnically() throws Exception {
    when(catalogServiceClient.findBookById(BOOK_ID))
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
  @DisplayName("Should keep percentage null when canonical page count is unknown")
  void shouldKeepPercentageNullWhenPageCountUnknown() throws Exception {
    when(catalogServiceClient.findBookById(UNKNOWN_PAGE_COUNT_BOOK_ID))
        .thenReturn(
            Optional.of(new CatalogBook(UNKNOWN_PAGE_COUNT_BOOK_ID, "Unknown", null, null)));

    final MvcResult addResult =
        mockMvc
            .perform(
                post("/api/v1/library/books")
                    .with(authenticatedJwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"bookId": "00000000-0000-0000-0000-000000000099", "initialState": "WANT_TO_READ"}
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.progress.percentage").value((Object) null))
            .andReturn();

    final String entryIdStr =
        com.jayway.jsonpath.JsonPath.read(
            addResult.getResponse().getContentAsString(), "$.entryId");
    final UUID entryId = UUID.fromString(entryIdStr);

    mockMvc
        .perform(
            patch("/api/v1/library/books/{entryId}/progress", entryId)
                .with(authenticatedJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"pagesRead": 120}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pagesRead").value(120))
        .andExpect(jsonPath("$.completionPercentage").value((Object) null));

    final Optional<UserBookEntity> persisted = jpaUserBookRepository.findById(entryId);
    assertThat(persisted).isPresent();
    assertThat(persisted.get().getPagesRead()).isEqualTo(120);
    assertThat(persisted.get().getPercentage()).isNull();
  }
}
