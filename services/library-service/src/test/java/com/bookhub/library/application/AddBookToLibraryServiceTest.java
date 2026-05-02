package com.bookhub.library.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.bookhub.library.application.error.BookNotFoundInCatalogException;
import com.bookhub.library.application.error.CatalogIntegrationException;
import com.bookhub.library.application.error.DuplicateLibraryEntryException;
import com.bookhub.library.domain.BookSnapshot;
import com.bookhub.library.domain.ReadingState;
import com.bookhub.library.domain.UserBook;
import com.bookhub.library.domain.UserBookRepository;
import com.bookhub.library.infrastructure.client.CatalogBook;
import com.bookhub.library.infrastructure.client.CatalogServiceClient;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddBookToLibraryServiceTest {

  @Mock private UserBookRepository userBookRepository;
  @Mock private CatalogServiceClient catalogServiceClient;

  @InjectMocks private AddBookToLibraryService addBookToLibraryService;

  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Test
  @DisplayName("Should add book to library when book exists in catalog")
  void shouldAddBookWhenItExistsInCatalog() {
    when(catalogServiceClient.findBookById(BOOK_ID))
        .thenReturn(Optional.of(new CatalogBook(BOOK_ID, "Clean Code", null, 464)));
    when(userBookRepository.findByUserIdAndBookId(USER_ID, BOOK_ID)).thenReturn(Optional.empty());
    when(userBookRepository.save(any(UserBook.class)))
        .thenAnswer(
            invocation -> {
              final UserBook ub = invocation.getArgument(0);
              ub.setId(UUID.randomUUID());
              return ub;
            });

    final UserBook result =
        addBookToLibraryService.execute(USER_ID, BOOK_ID, ReadingState.WANT_TO_READ);

    assertThat(result.getUserId()).isEqualTo(USER_ID);
    assertThat(result.getBookId()).isEqualTo(BOOK_ID);
    assertThat(result.getState()).isEqualTo(ReadingState.WANT_TO_READ);
    assertThat(result.getBook().title()).isEqualTo("Clean Code");
    assertThat(result.getBook().pageCount()).isEqualTo(464);
  }

  @Test
  @DisplayName("Should throw when book does not exist in catalog")
  void shouldThrowWhenBookNotFoundInCatalog() {
    when(catalogServiceClient.findBookById(BOOK_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> addBookToLibraryService.execute(USER_ID, BOOK_ID, ReadingState.WANT_TO_READ))
        .isInstanceOf(BookNotFoundInCatalogException.class);
  }

  @Test
  @DisplayName("Should throw when book is already in library")
  void shouldThrowWhenDuplicateEntry() {
    when(catalogServiceClient.findBookById(BOOK_ID))
        .thenReturn(Optional.of(new CatalogBook(BOOK_ID, "Clean Code", null, 464)));
    final UserBook existing =
        UserBook.create(
            USER_ID, BOOK_ID, ReadingState.WANT_TO_READ, new BookSnapshot("Clean Code", null, 464));
    when(userBookRepository.findByUserIdAndBookId(USER_ID, BOOK_ID))
        .thenReturn(Optional.of(existing));

    assertThatThrownBy(
            () -> addBookToLibraryService.execute(USER_ID, BOOK_ID, ReadingState.WANT_TO_READ))
        .isInstanceOf(DuplicateLibraryEntryException.class);
  }

  @Test
  @DisplayName("Should propagate technical catalog failures")
  void shouldPropagateTechnicalCatalogFailures() {
    when(catalogServiceClient.findBookById(BOOK_ID))
        .thenThrow(new CatalogIntegrationException("Catalog timeout"));

    assertThatThrownBy(
            () -> addBookToLibraryService.execute(USER_ID, BOOK_ID, ReadingState.WANT_TO_READ))
        .isInstanceOf(CatalogIntegrationException.class)
        .hasMessageContaining("Catalog timeout");
  }
}
