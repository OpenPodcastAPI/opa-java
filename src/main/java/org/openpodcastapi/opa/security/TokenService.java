package org.openpodcastapi.opa.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.openpodcastapi.opa.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    // The secret string used to generate secret keys
    @Value("${jwt.secret}")
    private String secret;

    // The TTL for each JWT, in minutes
    @Value("${jwt.expiration-minutes:15}")
    private long accessTokenMinutes;

    // The TTL for each refresh token, in days
    @Value("${jwt.refresh-days:7}")
    private long refreshTokenDays;

    // The calculated secret key
    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /// Generates an access token for a given user
    ///
    /// @param user the [User] to generate a token for
    /// @return the generated token
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUuid().toString())
                .claim("username", user.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenMinutes * 60)))
                .signWith(key())
                .compact();
    }

    /// Generates a refresh token for a given user
    ///
    /// @param user the [User] to generate a refresh token for
    /// @return the generated refresh token
    public String generateRefreshToken(User user) {
        String raw = UUID.randomUUID().toString() + UUID.randomUUID();
        String hash = passwordEncoder.encode(raw);

        RefreshToken token = RefreshToken.builder()
                .tokenHash(hash)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(refreshTokenDays * 24 * 3600))
                .build();

        repository.save(token);
        return raw;
    }

    /// Validates the refresh token for a user and updates its expiry time
    ///
    /// @param rawToken the raw token to validate
    /// @param user     the [User] to validate the token for
    /// @return the validated [User]
    public User validateRefreshToken(String rawToken, User user) {
        // Only fetch refresh tokens for the requesting user
        for (RefreshToken token : repository.findAllByUser(user)) {
            // Check that the raw token and the token hash match and the token is not expired
            if (passwordEncoder.matches(rawToken, token.getTokenHash()) &&
                    token.getExpiresAt().isAfter(Instant.now())) {
                // Update the expiry date on the refresh token
                token.setExpiresAt(Instant.now().plusSeconds(refreshTokenDays * 24 * 3600));
                RefreshToken updatedToken = repository.save(token);

                // Return the user to confirm the token is valid
                return updatedToken.getUser();
            }
        }
        throw new IllegalArgumentException("Invalid refresh token");
    }
}

