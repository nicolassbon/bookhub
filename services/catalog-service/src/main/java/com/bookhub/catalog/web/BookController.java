package com.bookhub.catalog.web;

import com.bookhub.catalog.application.GetBookDetailService;
import com.bookhub.catalog.application.SearchBooksService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  private static final int MAX_LIMIT = 100;

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
  public List<BookSearchResponse> search(
      @RequestParam("q") @NotBlank @Size(min = 2, max = 200) final String query,
      @RequestParam(value = "limit", defaultValue = "20") @Min(1) @Max(MAX_LIMIT) final int limit,
      @RequestParam(value = "offset", defaultValue = "0") @Min(0) final int offset) {
    return searchBooksService.search(query, limit, offset).stream()
        .map(bookWebMapper::toSearchResponse)
        .toList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getDetail(@PathVariable("id") final String id) {
    final GetBookDetailService.BookDetailResult result = getBookDetailService.getById(id);
    if (result.isDegraded()) {
      final Integer retryAfterSeconds = result.degraded().retryAfterSeconds();
      final ResponseEntity.BodyBuilder responseBuilder =
          ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE);
      if (retryAfterSeconds != null) {
        responseBuilder.header(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
      }
      return responseBuilder.body(bookWebMapper.toDegradedDetailResponse(result.degraded()));
    }
    return ResponseEntity.ok(bookWebMapper.toDetailResponse(result.book()));
  }
}
