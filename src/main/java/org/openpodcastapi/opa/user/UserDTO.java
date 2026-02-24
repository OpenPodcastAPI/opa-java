package org.openpodcastapi.opa.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/// Container for all user-related data transfer objects
public class UserDTO {
    /// A DTO representing a user response over the api
    ///
    /// @param uuid      the UUID of the user
    /// @param username  the username of the user
    /// @param email     the email address of the user
    /// @param createdAt the timestamp at which the user was created
    /// @param updatedAt the timestamp at which the user was last updated
    public record UserResponseDTO(
            @JsonProperty(required = true) UUID uuid,
            @JsonProperty(required = true) String username,
            @JsonProperty(required = true) String email,
            @JsonProperty(required = true) Instant createdAt,
            @JsonProperty(required = true) Instant updatedAt
    ) {
    }

    /// A DTO representing a new user
    ///
    /// @param email    the user's email address
    /// @param username the user's username
    /// @param password the user's unhashed password
    public record CreateUserDTO(
            @JsonProperty(required = true) @NotNull String username,
            @JsonProperty(required = true) @NotNull String password,
            @JsonProperty(required = true) @NotNull @Email String email
    ) {
    }
}
