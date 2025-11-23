package org.openpodcastapi.opa.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/// All data transfer objects for auth methods
public class AuthDTO {
    /// A DTO representing an api login request
    ///
    /// @param username the user's username
    /// @param password the user's password
    public record LoginRequest(
            @JsonProperty(value = "username", required = true) @NotNull String username,
            @JsonProperty(value = "password", required = true) @NotNull String password
    ) {
    }

    /// A DTO representing a successful api authentication attempt
    ///
    /// @param accessToken  the access token to be used to authenticate
    /// @param expiresIn    the TTL of the access token (in seconds)
    /// @param refreshToken the refresh token to be used to request new access tokens
    public record LoginSuccessResponse(
            @JsonProperty(value = "accessToken", required = true) @NotNull String accessToken,
            @JsonProperty(value = "refreshToken", required = true) @NotNull String refreshToken,
            @JsonProperty(value = "expiresIn", required = true) @NotNull String expiresIn
    ) {
    }

    /// A DTO representing a refresh token request
    ///
    /// @param username     the username of the requesting user
    /// @param refreshToken the refresh token used to issue a new token
    public record RefreshTokenRequest(
            @JsonProperty(value = "username", required = true) @NotNull String username,
            @JsonProperty(value = "refreshToken", required = true) @NotNull String refreshToken
    ) {
    }

    /// A DTO representing an updated access token from the refresh endpoint
    ///
    /// @param accessToken the newly generated access token
    /// @param expiresIn   the TTL of the token (in seconds)
    public record RefreshTokenResponse(
            @JsonProperty(value = "accessToken", required = true) @NotNull String accessToken,
            @JsonProperty(value = "expiresIn", required = true) @NotNull String expiresIn
    ) {
    }

    /// Displays an auth error
    ///
    /// @param error   the error message
    /// @param message an additional description of the error
    public record ErrorMessageDTO(
            @JsonProperty(value = "error", required = true) @NotNull String error,
            @JsonProperty(value = "message", required = true) @NotNull String message
    ) {
    }
}
