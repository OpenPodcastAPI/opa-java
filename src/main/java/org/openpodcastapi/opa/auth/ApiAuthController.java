package org.openpodcastapi.opa.auth;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openpodcastapi.opa.security.TokenService;
import org.openpodcastapi.opa.user.UserEntity;
import org.openpodcastapi.opa.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
public class ApiAuthController {
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/api/auth/login")
    public ResponseEntity<AuthDTO.LoginSuccessResponse> login(@RequestBody @NotNull AuthDTO.LoginRequest loginRequest) {
        // Set the authentication using the provided details
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );

        // Set the security context holder to the authenticated user
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Fetch the user record from the database
        UserEntity userEntity = userRepository.findByUsername(loginRequest.username()).orElseThrow(() -> new EntityNotFoundException("No userEntity with username " + loginRequest.username() + " found"));

        // Generate the access and refresh tokens for the user
        String accessToken = tokenService.generateAccessToken(userEntity);
        String refreshToken = tokenService.generateRefreshToken(userEntity);

        // Format the tokens and expiration time into a DTO
        AuthDTO.LoginSuccessResponse response = new AuthDTO.LoginSuccessResponse(accessToken, refreshToken, String.valueOf(tokenService.getExpirationTime()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<AuthDTO.RefreshTokenResponse> getRefreshToken(@RequestBody @NotNull AuthDTO.RefreshTokenRequest refreshTokenRequest) {
        UserEntity targetUserEntity = userRepository.findByUsername(refreshTokenRequest.username()).orElseThrow(() -> new EntityNotFoundException("No user with username " + refreshTokenRequest.username() + " found"));

        // Validate the existing refresh token
        UserEntity userEntity = tokenService.validateRefreshToken(refreshTokenRequest.refreshToken(), targetUserEntity);

        // Generate new access token
        String newAccessToken = tokenService.generateAccessToken(userEntity);

        // Format the token and expiration time into a DTO
        AuthDTO.RefreshTokenResponse response = new AuthDTO.RefreshTokenResponse(newAccessToken, String.valueOf(tokenService.getExpirationTime()));

        return ResponseEntity.ok(response);
    }
}

