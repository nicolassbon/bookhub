package com.bookhub.catalog.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.catalog.application.GetBookDetailService;
import com.bookhub.catalog.application.SearchBooksService;
import com.bookhub.catalog.application.error.BookNotFoundException;
import com.bookhub.catalog.application.model.BookSearchItem;
import com.bookhub.catalog.domain.Book;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
@Import({GlobalExceptionHandler.class, BookWebMapperImpl.class})
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchBooksService searchBooksService;

    @MockitoBean
    private GetBookDetailService getBookDetailService;

    @Test
    void shouldReturnSearchResults() throws Exception {
        final BookSearchItem item = new BookSearchItem(
                "ext:ol:OL262758W",
                "The Hobbit",
                "J.R.R. Tolkien",
                null,
                null,
                "https://covers.openlibrary.org/b/id/1-L.jpg",
                null);

        when(searchBooksService.search("hobbit")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/books").queryParam("q", "hobbit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("ext:ol:OL262758W"))
                .andExpect(jsonPath("$[0].title").value("The Hobbit"));
    }

    @Test
    void shouldReturnBookDetail() throws Exception {
        final UUID bookId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        final Book book = Book.builder()
                .id(bookId)
                .title("The Hobbit")
                .authorName("J.R.R. Tolkien")
                .sourceReference("OL262758W")
                .isbn13("9780261103344")
                .build();

        when(getBookDetailService.getById(bookId.toString())).thenReturn(book);

        mockMvc.perform(get("/api/v1/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId.toString()))
                .andExpect(jsonPath("$.sourceReference").value("OL262758W"));
    }

    @Test
    void shouldReturnNotFoundErrorShape() throws Exception {
        when(getBookDetailService.getById("ext:ol:OL404W"))
                .thenThrow(new BookNotFoundException("Book not found"));

        mockMvc.perform(get("/api/v1/books/{id}", "ext:ol:OL404W"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/v1/books/ext:ol:OL404W"));
    }
}
