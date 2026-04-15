package com.bookhub.catalog.web;

import com.bookhub.catalog.application.error.BookNotFoundException;
import com.bookhub.catalog.application.error.ExternalProviderException;
import com.bookhub.catalog.application.error.InvalidBookIdException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidBookIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBookId(
            final InvalidBookIdException exception,
            final HttpServletRequest request) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "INVALID_BOOK_ID",
                exception.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFound(
            final BookNotFoundException exception,
            final HttpServletRequest request) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "Not Found",
                "BOOK_NOT_FOUND",
                exception.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(ExternalProviderException.class)
    public ResponseEntity<ErrorResponse> handleExternalProvider(
            final ExternalProviderException exception,
            final HttpServletRequest request) {
        return buildError(
                HttpStatus.BAD_GATEWAY,
                "Bad Gateway",
                "EXTERNAL_PROVIDER_ERROR",
                exception.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandled(
            final Exception exception,
            final HttpServletRequest request) {
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
                .body(new ErrorResponse(
                        Instant.now(),
                        status.value(),
                        error,
                        code,
                        message,
                        path));
    }
}
