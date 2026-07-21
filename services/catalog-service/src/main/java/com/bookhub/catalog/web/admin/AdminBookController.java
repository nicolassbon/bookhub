package com.bookhub.catalog.web.admin;

import com.bookhub.catalog.application.admin.ImportBookService;
import com.bookhub.catalog.application.admin.ListBooksAdminService;
import com.bookhub.catalog.application.admin.UpdateBookService;
import com.bookhub.catalog.domain.Book;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/books")
public class AdminBookController {

  private final ListBooksAdminService listBooksAdminService;
  private final UpdateBookService updateBookService;
  private final ImportBookService importBookService;

  public AdminBookController(
      final ListBooksAdminService listBooksAdminService,
      final UpdateBookService updateBookService,
      final ImportBookService importBookService) {
    this.listBooksAdminService = listBooksAdminService;
    this.updateBookService = updateBookService;
    this.importBookService = importBookService;
  }

  @GetMapping
  public ResponseEntity<PagedAdminBookResponse> listBooks(
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "20") final int size,
      @RequestParam(required = false) final String source) {

    final ListBooksAdminService.PagedBooksResult result;
    if (source != null && !source.isBlank()) {
      result = listBooksAdminService.list(page, size, source);
    } else {
      result = listBooksAdminService.list(page, size);
    }
    final List<AdminBookResponse> items =
        result.books().stream().map(AdminBookResponse::from).toList();

    return ResponseEntity.ok(
        new PagedAdminBookResponse(
            items, result.page(), result.size(), result.totalElements(), result.totalPages()));
  }

  @PatchMapping("/{bookId}")
  public ResponseEntity<AdminBookResponse> updateBook(
      @PathVariable final UUID bookId, @RequestBody final UpdateBookRequest request) {

    final UpdateBookService.UpdateBookCommand command =
        new UpdateBookService.UpdateBookCommand(
            request.title(),
            request.authorName(),
            request.isbn13(),
            request.coverUrl(),
            request.publishedYear(),
            request.pageCount());

    final Book updated = updateBookService.update(bookId, command);
    return ResponseEntity.ok(AdminBookResponse.from(updated));
  }

  @PostMapping("/import")
  public ResponseEntity<AdminBookResponse> importBook(
      @Valid @RequestBody final ImportBookRequest request) {

    final ImportBookService.ImportBookCommand command =
        new ImportBookService.ImportBookCommand(request.sourceReference(), request.provider());

    final ImportBookService.ImportBookResult result = importBookService.importBook(command);
    final HttpStatus status = result.existed() ? HttpStatus.OK : HttpStatus.CREATED;
    return ResponseEntity.status(status).body(AdminBookResponse.from(result.book()));
  }
}
