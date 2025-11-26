package org.openpodcastapi.opa.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import org.openpodcastapi.opa.service.CustomUserDetails;
import org.openpodcastapi.opa.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/// Handles provisioning and authenticating JWTs for API requests
@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository repository;
    private final SecretKey key;

    /// Constructor with secret value provided in `.env` file
    /// or environment variables.
    ///
    /// @param repository the [UserRepository] interface for user entities
    /// @param secret     the secret value used to generate JWT values
    public JwtAuthenticationProvider(
            UserRepository repository,
            @Value("${jwt.secret}") String secret) {

        this.repository = repository;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        // Get the JWT token from the authentication header
        final var token = (String) authentication.getCredentials();

        try {
            // Parse the JWT claims
            final var claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Get the user's UUID from the claims subject
            final var uuid = UUID.fromString(claims.getSubject());

            // Find the user entity
            final var user = repository.findUserByUuid(uuid)
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            // Configure the user details for the authenticated user
            final var details = new CustomUserDetails(
                    user.getId(), user.getUuid(), user.getUsername(),
                    user.getPassword(), user.getUserRoles()
            );

            // Return the parsed token
            return new UsernamePasswordAuthenticationToken(
                    details, token, details.getAuthorities());
        } catch (Exception ex) {
            throw new BadCredentialsException("Invalid JWT: " + ex.getMessage());
        }
    }

    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

