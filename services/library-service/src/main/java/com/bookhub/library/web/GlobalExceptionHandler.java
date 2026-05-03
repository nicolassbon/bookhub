package com.bookhub.library.web;

import com.bookhub.library.application.error.BookNotFoundInCatalogException;
import com.bookhub.library.application.error.BookNotReadException;
import com.bookhub.library.application.error.CatalogIntegrationException;
import com.bookhub.library.application.error.DuplicateLibraryEntryException;
import com.bookhub.library.application.error.LibraryEntryNotFoundException;
import com.bookhub.library.application.error.LibraryEntryOwnershipException;
import com.bookhub.library.application.error.NotificationNotFoundException;
import com.bookhub.library.application.error.NotificationOwnershipException;
import com.bookhub.library.application.error.ReviewAlreadyExistsException;
import com.bookhub.library.application.error.YearlyGoalNotFoundException;
import com.bookhub.library.domain.ReadingProgressException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      final MethodArgumentNotValidException exception, final HttpServletRequest request) {
    final String message =
        exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
    return buildError(
        HttpStatus.BAD_REQUEST,
        "Validation Error",
        "VALIDATION_ERROR",
        message,
        request.getRequestURI());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      final ConstraintViolationException exception, final HttpServletRequest request) {
    final String message =
        exception.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + " " + v.getMessage())
            .collect(Collectors.joining("; "));
    return buildError(
        HttpStatus.BAD_REQUEST,
        "Validation Error",
        "VALIDATION_ERROR",
        message,
        request.getRequestURI());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      final IllegalArgumentException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.BAD_REQUEST,
        "Validation Error",
        "INVALID_ARGUMENT",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(BookNotFoundInCatalogException.class)
  public ResponseEntity<ErrorResponse> handleBookNotFoundInCatalog(
      final BookNotFoundInCatalogException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        "BOOK_NOT_FOUND_IN_CATALOG",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(CatalogIntegrationException.class)
  public ResponseEntity<ErrorResponse> handleCatalogIntegrationException(
      final CatalogIntegrationException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.BAD_GATEWAY,
        "Bad Gateway",
        "CATALOG_INTEGRATION_ERROR",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(DuplicateLibraryEntryException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateLibraryEntry(
      final DuplicateLibraryEntryException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.CONFLICT,
        "Conflict",
        "DUPLICATE_LIBRARY_ENTRY",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(LibraryEntryNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleLibraryEntryNotFound(
      final LibraryEntryNotFoundException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.NOT_FOUND,
        "Not Found",
        "LIBRARY_ENTRY_NOT_FOUND",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(LibraryEntryOwnershipException.class)
  public ResponseEntity<ErrorResponse> handleLibraryEntryOwnership(
      final LibraryEntryOwnershipException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.FORBIDDEN,
        "Forbidden",
        "LIBRARY_ENTRY_OWNERSHIP",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(YearlyGoalNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleYearlyGoalNotFound(
      final YearlyGoalNotFoundException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.NOT_FOUND,
        "Not Found",
        "YEARLY_GOAL_NOT_FOUND",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(ReadingProgressException.class)
  public ResponseEntity<ErrorResponse> handleReadingProgress(
      final ReadingProgressException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        "INVALID_READING_PROGRESS",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(BookNotReadException.class)
  public ResponseEntity<ErrorResponse> handleBookNotRead(
      final BookNotReadException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        "BOOK_NOT_READ",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(ReviewAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleReviewAlreadyExists(
      final ReviewAlreadyExistsException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.CONFLICT,
        "Conflict",
        "REVIEW_ALREADY_EXISTS",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(NotificationNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotificationNotFound(
      final NotificationNotFoundException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.NOT_FOUND,
        "Not Found",
        "NOTIFICATION_NOT_FOUND",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(NotificationOwnershipException.class)
  public ResponseEntity<ErrorResponse> handleNotificationOwnership(
      final NotificationOwnershipException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.FORBIDDEN,
        "Forbidden",
        "NOTIFICATION_OWNERSHIP_VIOLATION",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
      final MethodArgumentTypeMismatchException exception, final HttpServletRequest request) {
    final String message =
        String.format(
            "Invalid value '%s' for parameter '%s'", exception.getValue(), exception.getName());
    return buildError(
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        "INVALID_PARAMETER",
        message,
        request.getRequestURI());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFound(
      final NoResourceFoundException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.NOT_FOUND,
        "Not Found",
        "RESOURCE_NOT_FOUND",
        "Resource not found",
        request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnhandled(
      final Exception exception, final HttpServletRequest request) {
    log.error(
        "Unhandled server exception method={} path={}",
        request.getMethod(),
        request.getRequestURI(),
        exception);
    return buildError(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error",
        "INTERNAL_ERROR",
        "Unexpected error",
        request.getRequestURI());
  }

  private ResponseEntity<ErrorResponse> buildError(
      final HttpStatus status,
      final String error,
      final String code,
      final String message,
      final String path) {
    return ResponseEntity.status(status)
        .body(new ErrorResponse(Instant.now(), status.value(), error, code, message, path));
  }
}
