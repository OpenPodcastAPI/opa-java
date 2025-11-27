package org.openpodcastapi.opa.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.openpodcastapi.opa.user.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/// Service for refresh token and JWT-related actions
@Service
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
    @Value("${jwt.ttl}")
    private String jwtExpiration;

    /// Required args constructor
    ///
    /// @param repository      the refresh token repository for token interaction
    /// @param passwordEncoder the password encoder for encoding tokens
    public TokenService(RefreshTokenRepository repository, BCryptPasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /// The calculated secret key
    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /// Calculates the token expiry date from a given timestamp
    ///
    /// @param fromDate the date from which to calculate the expiry
    /// @return a formatted expiry date
    private Date calculateAccessTokenExpiryDate(Instant fromDate) {
        return Date.from(fromDate.plusSeconds(accessTokenMinutes * 60));
    }

    /// Calculates the refresh token expiry time from a given timestamp
    ///
    /// @param fromDate the date from which to calculate the expiry
    /// @return the time to expiry in seconds
    private Instant calculateRefreshTokenExpiry(Instant fromDate) {
        return fromDate.plusSeconds(refreshTokenDays * 24 * 3600);
    }

    /// Returns the expiration time for JWTs
    ///
    /// @return a number representing the user-defined TTL of JWT tokens
    public long getExpirationTime() {
        return Long.parseLong(jwtExpiration);
    }

    /// Generates an access token for a given user
    ///
    /// @param userEntity the user to generate a token for
    /// @return the generated token
    public String generateAccessToken(UserEntity userEntity) {
        final var now = Instant.now();
        return Jwts.builder()
                .subject(userEntity.getUuid().toString())
                .claim("username", userEntity.getUsername())
                .issuedAt(Date.from(now))
                .expiration(calculateAccessTokenExpiryDate(Instant.now()))
                .signWith(key())
                .compact();
    }

    /// Generates a refresh token for a given user
    ///
    /// @param userEntity the user to generate a refresh token for
    /// @return the generated refresh token
    public String generateRefreshToken(UserEntity userEntity) {
        final var raw = UUID.randomUUID().toString() + UUID.randomUUID();
        final var hash = passwordEncoder.encode(raw);
        final var expiryDate = calculateRefreshTokenExpiry(Instant.now());

        final var token = new RefreshTokenEntity(hash, userEntity, expiryDate);

        repository.save(token);
        return raw;
    }

    /// Validates the refresh token for a user and updates its expiry time
    ///
    /// @param rawToken   the raw token to validate
    /// @param userEntity the user to validate the token for
    /// @return the validated user
    public UserEntity validateRefreshToken(String rawToken, UserEntity userEntity) {
        // Only fetch refresh tokens for the requesting user
        for (RefreshTokenEntity token : repository.findAllByUser(userEntity)) {
            // Check that the raw token and the token hash match and the token is not expired
            if (passwordEncoder.matches(rawToken, token.getTokenHash()) &&
                    token.getExpiresAt().isAfter(Instant.now())) {
                // Update the expiry date on the refresh token
                token.setExpiresAt(calculateRefreshTokenExpiry(Instant.now()));
                final var updatedToken = repository.save(token);

                // Return the user to confirm the token is valid
                return updatedToken.getUser();
            }
        }
        throw new IllegalArgumentException("Invalid refresh token");
    }
}

