package org.openpodcastapi.opa.advice;

import jakarta.persistence.EntityNotFoundException;
import org.jspecify.annotations.NonNull;
import org.openpodcastapi.opa.exceptions.ValidationErrorResponse;
import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

import static org.slf4j.LoggerFactory.getLogger;

/// A global handler for common exceptions thrown by the application.
///
/// Where possible, controllers should throw their own exceptions.
/// However, for common exceptions such as invalid parameters and
/// not found entities, a global exception handler can be added.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = getLogger(GlobalExceptionHandler.class);

    /// Returns a 404 if a database entity is not found
    ///
    /// @param exception the thrown exception
    /// @return a response containing the error message
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<@NonNull String> handleEntityNotFoundException(EntityNotFoundException exception) {
        log.info("{}", exception.getMessage());
        return ResponseEntity.notFound().build();
    }

    /// Returns a 400 error when conflicting data is entered
    ///
    /// @param exception the thrown exception
    /// @return a response containing the error message
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<@NonNull String> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    /// Returns a 400 error when illegal arguments are passed
    ///
    /// @param exception the thrown exception
    /// @return a response containing the error message
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<@NonNull String> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    /// Returns a 400 error when invalid arguments are passed to an endpoint
    ///
    /// @param exception the thrown exception
    /// @return a response containing the error message
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        final var errors = exception.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ValidationErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        final var body = new ValidationErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                errors
        );

        return ResponseEntity.badRequest().body(body);
    }
}
