package com.bookhub.catalog.web;

import com.bookhub.catalog.application.GetBookDetailService;
import com.bookhub.catalog.application.SearchBooksService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final SearchBooksService searchBooksService;
    private final GetBookDetailService getBookDetailService;
    private final BookWebMapper bookWebMapper;

    public BookController(
            final SearchBooksService searchBooksService,
            final GetBookDetailService getBookDetailService,
            final BookWebMapper bookWebMapper) {
        this.searchBooksService = searchBooksService;
        this.getBookDetailService = getBookDetailService;
        this.bookWebMapper = bookWebMapper;
    }

    @GetMapping
    public List<BookSearchResponse> search(@RequestParam("q") @NotBlank final String query) {
        return searchBooksService.search(query)
                .stream()
                .map(bookWebMapper::toSearchResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public BookDetailResponse getDetail(@PathVariable("id") final String id) {
        return bookWebMapper.toDetailResponse(getBookDetailService.getById(id));
    }
}
