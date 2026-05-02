package com.bookhub.library.web.library;

import com.bookhub.library.application.AddBookToLibraryService;
import com.bookhub.library.application.GetUserLibraryService;
import com.bookhub.library.application.UpdateReadingProgressService;
import com.bookhub.library.application.UpdateReadingStateService;
import com.bookhub.library.domain.ReadingState;
import com.bookhub.library.domain.UserBook;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/library")
@RequiredArgsConstructor
public class LibraryController {

  private final AddBookToLibraryService addBookToLibraryService;
  private final GetUserLibraryService getUserLibraryService;
  private final UpdateReadingStateService updateReadingStateService;
  private final UpdateReadingProgressService updateReadingProgressService;

  @PostMapping("/books")
  public ResponseEntity<LibraryEntryResponse> addBookToLibrary(
      @AuthenticationPrincipal final Jwt jwt, @Valid @RequestBody final AddBookRequest request) {
    final UUID userId = UUID.fromString(jwt.getSubject());
    final UserBook userBook =
        addBookToLibraryService.execute(userId, request.bookId(), request.initialState());
    return ResponseEntity.status(HttpStatus.CREATED).body(LibraryEntryResponse.from(userBook));
  }

  @GetMapping("/me")
  public ResponseEntity<LibrarySummaryResponse> getLibrarySummary(
      @AuthenticationPrincipal final Jwt jwt) {
    final UUID userId = UUID.fromString(jwt.getSubject());
    final GetUserLibraryService.LibrarySummary summary =
        getUserLibraryService.getLibrarySummary(userId);
    return ResponseEntity.ok(
        new LibrarySummaryResponse(
            summary.total(), summary.wantToRead(), summary.reading(), summary.read()));
  }

  @GetMapping("/me/books")
  public ResponseEntity<List<LibraryEntryResponse>> getLibraryBooks(
      @AuthenticationPrincipal final Jwt jwt,
      @RequestParam(required = false) final ReadingState state) {
    final UUID userId = UUID.fromString(jwt.getSubject());
    final List<UserBook> entries;
    if (state != null) {
      entries = getUserLibraryService.getLibraryEntriesByState(userId, state);
    } else {
      entries = getUserLibraryService.getLibraryEntries(userId);
    }
    return ResponseEntity.ok(entries.stream().map(LibraryEntryResponse::from).toList());
  }

  @GetMapping("/books/{entryId}")
  public ResponseEntity<LibraryEntryResponse> getLibraryEntry(
      @AuthenticationPrincipal final Jwt jwt, @PathVariable final UUID entryId) {
    final UUID userId = UUID.fromString(jwt.getSubject());
    final UserBook entry = getUserLibraryService.getLibraryEntry(userId, entryId);
    return ResponseEntity.ok(LibraryEntryResponse.from(entry));
  }

  @PatchMapping("/books/{entryId}/state")
  public ResponseEntity<LibraryEntryResponse> updateReadingState(
      @AuthenticationPrincipal final Jwt jwt,
      @PathVariable final UUID entryId,
      @Valid @RequestBody final UpdateStateRequest request) {
    final UUID userId = UUID.fromString(jwt.getSubject());
    final UserBook updated = updateReadingStateService.execute(userId, entryId, request.state());
    return ResponseEntity.ok(LibraryEntryResponse.from(updated));
  }

  @PatchMapping("/books/{entryId}/progress")
  public ResponseEntity<UpdateProgressResponse> updateReadingProgress(
      @AuthenticationPrincipal final Jwt jwt,
      @PathVariable final UUID entryId,
      @Valid @RequestBody final UpdateProgressRequest request) {
    final UUID userId = UUID.fromString(jwt.getSubject());
    final UserBook updated =
        updateReadingProgressService.execute(userId, entryId, request.pagesRead());
    return ResponseEntity.ok(UpdateProgressResponse.from(updated));
  }
}
