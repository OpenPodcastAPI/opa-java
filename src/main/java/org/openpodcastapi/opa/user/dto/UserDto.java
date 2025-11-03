package org.openpodcastapi.opa.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/// A DTO representing a user response over the API
///
/// @param uuid      the UUID of the user
/// @param username  the username of the user
/// @param email     the email address of the user
/// @param createdAt the timestamp at which the user was created
/// @param updatedAt the timestamp at which the user was last updated
public record UserDto(
        @JsonProperty(required = true) UUID uuid,
        @JsonProperty(required = true) String username,
        @JsonProperty(required = true) String email,
        @JsonProperty(required = true) Instant createdAt,
        @JsonProperty(required = true) Instant updatedAt
) {
}
