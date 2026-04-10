package com.bookhub.identity.web.error;

import com.bookhub.identity.application.auth.InvalidCredentialsException;
import com.bookhub.identity.domain.user.DuplicateResourceException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Comparator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            final MethodArgumentNotValidException exception,
            final HttpServletRequest request) {
        final String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Request validation failed");

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                "VALIDATION_ERROR",
                message,
                request.getRequestURI());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            final DuplicateResourceException exception,
            final HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                "DUPLICATE_RESOURCE",
                exception.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            final DataIntegrityViolationException exception,
            final HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                "DUPLICATE_RESOURCE",
                "Email or username already in use",
                request.getRequestURI());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            final InvalidCredentialsException exception,
            final HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "INVALID_CREDENTIALS",
                exception.getMessage(),
                request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            final HttpStatus status,
            final String error,
            final String code,
            final String message,
            final String path) {
        final ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .code(code)
                .message(message)
                .path(path)
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
