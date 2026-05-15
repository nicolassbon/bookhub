package com.bookhub.catalog.web;

import com.bookhub.catalog.application.error.BookNotFoundException;
import com.bookhub.catalog.application.error.ExternalProviderException;
import com.bookhub.catalog.application.error.InvalidBookIdException;
import com.bookhub.catalog.application.error.InvalidProviderPayloadException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      final org.springframework.web.bind.MethodArgumentNotValidException exception, final HttpServletRequest request) {
    final String message =
        exception.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + " " + e.getDefaultMessage())
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

  @ExceptionHandler(InvalidBookIdException.class)
  public ResponseEntity<ErrorResponse> handleInvalidBookId(
      final InvalidBookIdException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        "INVALID_BOOK_ID",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(BookNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleBookNotFound(
      final BookNotFoundException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.NOT_FOUND,
        "Not Found",
        "BOOK_NOT_FOUND",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(ExternalProviderException.class)
  public ResponseEntity<ErrorResponse> handleExternalProvider(
      final ExternalProviderException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.BAD_GATEWAY,
        "Bad Gateway",
        "EXTERNAL_PROVIDER_ERROR",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(InvalidProviderPayloadException.class)
  public ResponseEntity<ErrorResponse> handleInvalidProviderPayload(
      final InvalidProviderPayloadException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.BAD_GATEWAY,
        "Bad Gateway",
        "INVALID_PROVIDER_PAYLOAD",
        exception.getMessage(),
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

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(
      final HttpRequestMethodNotSupportedException exception, final HttpServletRequest request) {
    final String message =
        String.format("Method '%s' is not supported for this endpoint", exception.getMethod());
    return buildError(
        HttpStatus.METHOD_NOT_ALLOWED,
        "Method Not Allowed",
        "METHOD_NOT_ALLOWED",
        message,
        request.getRequestURI());
  }

  @ExceptionHandler(com.bookhub.catalog.application.admin.AdminImportDegradedException.class)
  public ResponseEntity<ErrorResponse> handleAdminImportDegraded(
      final com.bookhub.catalog.application.admin.AdminImportDegradedException exception, final HttpServletRequest request) {
    return buildError(
        HttpStatus.SERVICE_UNAVAILABLE,
        "Service Unavailable",
        "EXTERNAL_SERVICE_UNAVAILABLE",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnhandled(
      final Exception exception, final HttpServletRequest request) {
    log.error(
        "Unhandled server exception method={} path={} requestId={}",
        request.getMethod(),
        request.getRequestURI(),
        requestId(request),
        exception);

    return buildError(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error",
        "INTERNAL_ERROR",
        "Unexpected error",
        request.getRequestURI());
  }

  private String requestId(final HttpServletRequest request) {
    final Object requestId = request.getAttribute("requestId");
    return requestId == null ? "n/a" : requestId.toString();
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
