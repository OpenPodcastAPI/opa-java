package org.openpodcastapi.opa.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.openpodcastapi.opa.user.UserEntity;
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

    /// Generates an access token for a given userEntity
    ///
    /// @param userEntity the [UserEntity] to generate a token for
    /// @return the generated token
    public String generateAccessToken(UserEntity userEntity) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userEntity.getUuid().toString())
                .claim("username", userEntity.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenMinutes * 60)))
                .signWith(key())
                .compact();
    }

    /// Generates a refresh token for a given userEntity
    ///
    /// @param userEntity the [UserEntity] to generate a refresh token for
    /// @return the generated refresh token
    public String generateRefreshToken(UserEntity userEntity) {
        String raw = UUID.randomUUID().toString() + UUID.randomUUID();
        String hash = passwordEncoder.encode(raw);

        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .tokenHash(hash)
                .user(userEntity)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(refreshTokenDays * 24 * 3600))
                .build();

        repository.save(token);
        return raw;
    }

    /// Validates the refresh token for a userEntity and updates its expiry time
    ///
    /// @param rawToken   the raw token to validate
    /// @param userEntity the [UserEntity] to validate the token for
    /// @return the validated [UserEntity]
    public UserEntity validateRefreshToken(String rawToken, UserEntity userEntity) {
        // Only fetch refresh tokens for the requesting userEntity
        for (RefreshTokenEntity token : repository.findAllByUser(userEntity)) {
            // Check that the raw token and the token hash match and the token is not expired
            if (passwordEncoder.matches(rawToken, token.getTokenHash()) &&
                    token.getExpiresAt().isAfter(Instant.now())) {
                // Update the expiry date on the refresh token
                token.setExpiresAt(Instant.now().plusSeconds(refreshTokenDays * 24 * 3600));
                RefreshTokenEntity updatedToken = repository.save(token);

                // Return the userEntity to confirm the token is valid
                return updatedToken.getUser();
            }
        }
        throw new IllegalArgumentException("Invalid refresh token");
    }
}

