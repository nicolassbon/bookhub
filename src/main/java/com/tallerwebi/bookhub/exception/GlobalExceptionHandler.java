package com.tallerwebi.bookhub.exception;

import com.tallerwebi.bookhub.dto.response.ErrorResponse;
import com.tallerwebi.bookhub.exception.custom.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(
                        HttpStatus.NOT_FOUND,
                        "Resource Not Found",
                        ex.getMessage(),
                        null
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        "Validation Failed",
                        "Invalid request data",
                        details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex) {
        log.error("An unexpected internal server error ocurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        "An unexpected error occurred",
                        null));
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String error, String message, List<String> details) {
        return ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
