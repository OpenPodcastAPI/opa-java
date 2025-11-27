package org.openpodcastapi.opa.controllers.api;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;
import org.openpodcastapi.opa.auth.AuthDTO;
import org.openpodcastapi.opa.security.TokenService;
import org.openpodcastapi.opa.user.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/// Controllers for API-based authentication
@RestController
public class AuthController {
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    /// Constructs the controller with the correct [AuthenticationManager]
    ///
    /// @param tokenService          the [TokenService] used to manage auth tokens
    /// @param userRepository        the [UserRepository] used to manage user entity interaction
    /// @param authenticationManager the [AuthenticationManager] used to handle auth
    /// @see org.openpodcastapi.opa.config.SecurityConfig#apiLoginAuthenticationManager
    public AuthController(
            TokenService tokenService,
            UserRepository userRepository,
            @Qualifier("apiLoginManager") AuthenticationManager authenticationManager
    ) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    /// The API login endpoint. Accepts a basic username/password combination to authenticate.
    ///
    /// @param loginRequest the login request containing the user's credentials
    /// @return a success response
    @PostMapping("/api/auth/login")
    public ResponseEntity<AuthDTO.@NonNull LoginSuccessResponse> login(@RequestBody @NotNull AuthDTO.LoginRequest loginRequest) {
        // Set the authentication using the provided details
        final var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );

        // Set the security context holder to the authenticated user
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Fetch the user record from the database
        final var userEntity = userRepository.findUserByUsername(loginRequest.username()).orElseThrow(() -> new EntityNotFoundException("No userEntity with username " + loginRequest.username() + " found"));

        // Generate the access and refresh tokens for the user
        final String accessToken = tokenService.generateAccessToken(userEntity);
        final String refreshToken = tokenService.generateRefreshToken(userEntity);

        // Format the tokens and expiration time into a DTO
        final var response = new AuthDTO.LoginSuccessResponse(accessToken, refreshToken, String.valueOf(tokenService.getExpirationTime()));

        return ResponseEntity.ok(response);
    }

    /// The token refresh endpoint. Validates refresh tokens and returns new access tokens.
    ///
    /// @param refreshTokenRequest the refresh token request body
    /// @return a token refresh response
    @PostMapping("/api/auth/refresh")
    public ResponseEntity<AuthDTO.@NonNull RefreshTokenResponse> getRefreshToken(@RequestBody @NotNull AuthDTO.RefreshTokenRequest refreshTokenRequest) {
        final var targetUserEntity = userRepository.findUserByUsername(refreshTokenRequest.username()).orElseThrow(() -> new EntityNotFoundException("No user with username " + refreshTokenRequest.username() + " found"));

        // Validate the existing refresh token
        final var userEntity = tokenService.validateRefreshToken(refreshTokenRequest.refreshToken(), targetUserEntity);

        // Generate new access token
        final var newAccessToken = tokenService.generateAccessToken(userEntity);

        // Format the token and expiration time into a DTO
        final var response = new AuthDTO.RefreshTokenResponse(newAccessToken, String.valueOf(tokenService.getExpirationTime()));

        return ResponseEntity.ok(response);
    }
}

