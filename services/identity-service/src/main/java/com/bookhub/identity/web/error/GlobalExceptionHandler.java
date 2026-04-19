package com.bookhub.identity.web.error;

import com.bookhub.identity.application.auth.InvalidCredentialsException;
import com.bookhub.identity.application.auth.InvalidPasswordResetTokenException;
import com.bookhub.identity.application.auth.InvalidRefreshTokenException;
import com.bookhub.identity.domain.user.DuplicateResourceException;
import com.bookhub.identity.web.auth.ratelimit.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Comparator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingRequestCookieException;
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

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(
            final InvalidRefreshTokenException exception,
            final HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "INVALID_REFRESH_TOKEN",
                exception.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordResetToken(
            final InvalidPasswordResetTokenException exception,
            final HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "INVALID_PASSWORD_RESET_TOKEN",
                exception.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
            final RateLimitExceededException exception,
            final HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                "RATE_LIMIT_EXCEEDED",
                exception.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestCookie(
            final MissingRequestCookieException exception,
            final HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "MISSING_REQUEST_COOKIE",
                exception.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandled(
            final Exception exception,
            final HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "INTERNAL_ERROR",
                "Unexpected error",
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
