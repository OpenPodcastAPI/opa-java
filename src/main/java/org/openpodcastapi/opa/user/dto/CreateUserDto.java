package org.openpodcastapi.opa.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/// A DTO representing a new user
///
/// @param email    the user's email address
/// @param username the user's username
/// @param password the user's unhashed password
public record CreateUserDto(
        @JsonProperty(required = true) @NotNull String username,
        @JsonProperty(required = true) @NotNull String password,
        @JsonProperty(required = true) @NotNull @Email String email
) {
}
