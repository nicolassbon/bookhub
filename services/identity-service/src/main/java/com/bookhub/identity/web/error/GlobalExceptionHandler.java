package com.bookhub.identity.web.error;

import com.bookhub.identity.application.auth.InvalidCredentialsException;
import com.bookhub.identity.application.auth.InvalidPasswordResetTokenException;
import com.bookhub.identity.application.auth.InvalidRefreshTokenException;
import com.bookhub.identity.application.auth.InvalidServiceCredentialsException;
import com.bookhub.identity.application.auth.ratelimit.AuthRateLimitStoreUnavailableException;
import com.bookhub.identity.domain.user.DuplicateResourceException;
import com.bookhub.identity.domain.user.UserNotFoundException;
import com.bookhub.identity.web.admin.InvalidRoleException;
import com.bookhub.identity.web.auth.ratelimit.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      final MethodArgumentNotValidException exception, final HttpServletRequest request) {
    final String message =
        exception.getBindingResult().getFieldErrors().stream()
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

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadableRequestBody(
      final HttpMessageNotReadableException exception, final HttpServletRequest request) {
    final String errorMessage =
        isMissingRequestBody(exception) ? "Request body is required" : "Malformed JSON request";

    return buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        "MALFORMED_REQUEST",
        errorMessage,
        request.getRequestURI());
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateResource(
      final DuplicateResourceException exception, final HttpServletRequest request) {
    return buildErrorResponse(
        HttpStatus.CONFLICT,
        "Conflict",
        "DUPLICATE_RESOURCE",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
      final DataIntegrityViolationException exception, final HttpServletRequest request) {
    return buildErrorResponse(
        HttpStatus.CONFLICT,
        "Conflict",
        "DUPLICATE_RESOURCE",
        "Email or username already in use",
        request.getRequestURI());
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCredentials(
      final InvalidCredentialsException exception, final HttpServletRequest request) {
    return buildErrorResponse(
        HttpStatus.UNAUTHORIZED,
        "Unauthorized",
        "INVALID_CREDENTIALS",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(InvalidRefreshTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(
      final InvalidRefreshTokenException exception, final HttpServletRequest request) {
    return buildErrorResponse(
        HttpStatus.UNAUTHORIZED,
        "Unauthorized",
        "INVALID_REFRESH_TOKEN",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(InvalidServiceCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleInvalidServiceCredentials(
      final InvalidServiceCredentialsException exception, final HttpServletRequest request) {
    return buildErrorResponse(
        HttpStatus.UNAUTHORIZED,
        "Unauthorized",
        "UNAUTHORIZED",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(InvalidPasswordResetTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidPasswordResetToken(
      final InvalidPasswordResetTokenException exception, final HttpServletRequest request) {
    return buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        "INVALID_PASSWORD_RESET_TOKEN",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(RateLimitExceededException.class)
  public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
      final RateLimitExceededException exception, final HttpServletRequest request) {
    return buildErrorResponse(
        HttpStatus.TOO_MANY_REQUESTS,
        "Too Many Requests",
        "RATE_LIMIT_EXCEEDED",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(AuthRateLimitStoreUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleRateLimitStoreUnavailable(
      final AuthRateLimitStoreUnavailableException exception, final HttpServletRequest request) {
    log.warn(
        "Authentication rate-limit store unavailable method={} path={} requestId={}",
        request.getMethod(),
        request.getRequestURI(),
        requestId(request),
        exception);

    return buildErrorResponse(
        HttpStatus.SERVICE_UNAVAILABLE,
        "Service Unavailable",
        "AUTH_RATE_LIMIT_UNAVAILABLE",
        "Authentication rate limiting is temporarily unavailable",
        request.getRequestURI());
  }

  @ExceptionHandler(MissingRequestCookieException.class)
  public ResponseEntity<ErrorResponse> handleMissingRequestCookie(
      final MissingRequestCookieException exception, final HttpServletRequest request) {
    return buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        "MISSING_REQUEST_COOKIE",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFound(
      final NoResourceFoundException exception, final HttpServletRequest request) {
    return buildErrorResponse(
        HttpStatus.NOT_FOUND,
        "Not Found",
        "RESOURCE_NOT_FOUND",
        "Resource not found",
        request.getRequestURI());
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFound(
      final UserNotFoundException exception, final HttpServletRequest request) {
    return buildErrorResponse(
        HttpStatus.NOT_FOUND,
        "Not Found",
        "USER_NOT_FOUND",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(InvalidRoleException.class)
  public ResponseEntity<ErrorResponse> handleInvalidRole(
      final InvalidRoleException exception, final HttpServletRequest request) {
    return buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        "VALIDATION_ERROR",
        exception.getMessage(),
        request.getRequestURI());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      final IllegalArgumentException exception, final HttpServletRequest request) {
    return buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        "VALIDATION_ERROR",
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

    return buildErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error",
        "INTERNAL_ERROR",
        "Unexpected error",
        request.getRequestURI());
  }

  private boolean isMissingRequestBody(final HttpMessageNotReadableException exception) {
    return exception.getMessage() != null
        && exception.getMessage().contains("Required request body is missing");
  }

  private String requestId(final HttpServletRequest request) {
    final Object requestId = request.getAttribute("requestId");
    return requestId == null ? "n/a" : requestId.toString();
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(
      final HttpStatus status,
      final String error,
      final String code,
      final String message,
      final String path) {
    final ErrorResponse body =
        ErrorResponse.builder()
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
