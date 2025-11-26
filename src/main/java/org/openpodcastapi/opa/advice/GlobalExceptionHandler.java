package org.openpodcastapi.opa.advice;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openpodcastapi.opa.exceptions.ValidationErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/// A global handler for common exceptions thrown by the application.
///
/// Where possible, controllers should throw their own exceptions.
/// However, for common exceptions such as invalid parameters and
/// not found entities, a global exception handler can be added.
@RestControllerAdvice
@RequiredArgsConstructor
@Log4j2
public class GlobalExceptionHandler {
    /// Returns a 404 if a database entity is not found
    ///
    /// @param exception the thrown [EntityNotFoundException]
    /// @return a [ResponseEntity] containing the error message
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<@NonNull String> handleEntityNotFoundException(EntityNotFoundException exception) {
        log.debug("{}", exception.getMessage());
        return ResponseEntity.notFound().build();
    }

    /// Returns a 400 error when conflicting data is entered
    ///
    /// @param exception the thrown [DataIntegrityViolationException]
    /// @return a [ResponseEntity] containing the error message
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<@NonNull String> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    /// Returns a 400 error when illegal arguments are passed
    ///
    /// @param exception the thrown [IllegalArgumentException]
    /// @return a [ResponseEntity] containing the error message
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<@NonNull String> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    /// Returns a 400 error when invalid arguments are passed to an endpoint
    ///
    /// @param exception the thrown [MethodArgumentNotValidException]
    /// @return a [ResponseEntity] containing the error message
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        List<ValidationErrorResponse.FieldError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ValidationErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        var body = new ValidationErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                errors
        );

        return ResponseEntity.badRequest().body(body);
    }
}
