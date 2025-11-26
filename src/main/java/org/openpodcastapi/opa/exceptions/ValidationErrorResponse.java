package org.openpodcastapi.opa.exceptions;

import java.time.Instant;
import java.util.List;

/// The JSON representation of field validation errors
///
/// @param timestamp the timestamp at which the error occurred
/// @param status    the HTTP status code
/// @param errors    a list of [FieldError] objects
public record ValidationErrorResponse(
        Instant timestamp,
        int status,
        List<FieldError> errors
) {
    /// Represents an error message for field validation errors
    ///
    /// @param field   the field containing the error
    /// @param message the error message to display
    public record FieldError(String field, String message) {
    }
}
