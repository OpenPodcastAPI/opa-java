package org.openpodcastapi.opa.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openpodcastapi.opa.service.CustomUserDetails;
import org.openpodcastapi.opa.user.UserEntity;
import org.openpodcastapi.opa.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserRepository repository;
    // The JWT secret string set in the env file
    @Value("${jwt.secret}")
    private String jwtSecret;

    /// Returns an authentication token for a user
    ///
    /// @param userEntity the [UserEntity] to fetch a token for
    /// @return a generated token
    /// @throws EntityNotFoundException if no matching user is found
    private static UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(UserEntity userEntity) throws EntityNotFoundException {
        // Create a new CustomUserDetails entity with the fetched user
        CustomUserDetails userDetails =
                new CustomUserDetails(userEntity.getId(),
                        userEntity.getUuid(),
                        userEntity.getUsername(),
                        userEntity.getPassword(),
                        userEntity.getUserRoles());

        // Return a token for the user
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    /// Filter requests by token
    ///
    /// @param req   the HTTP request
    /// @param res   the HTTP response
    /// @param chain the filter chain
    /// @throws ServletException if the request can't be served
    /// @throws IOException      if an I/O issue is encountered
    @Override
    protected void doFilterInternal(HttpServletRequest req, @Nonnull HttpServletResponse res, @Nonnull FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        // If the value is missing or is not a valid bearer token, filter the response
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        // Check that a valid Bearer token is in the headers
        String token = header.substring(7);

        try {
            // Extract the claims from the JWT
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Extract the user's UUID from the claims
            String userUuid = claims.getSubject();
            UUID parsedUuid = UUID.fromString(userUuid);

            // Fetch the matching user
            UserEntity userEntity = repository.getUserByUuid(parsedUuid).orElseThrow(() -> new EntityNotFoundException("No matching user found"));

            // Create a user
            UsernamePasswordAuthenticationToken authentication = getUsernamePasswordAuthenticationToken(userEntity);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.error("Invalid token passed to endpoint: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token passed to endpoint");
        }

        chain.doFilter(req, res);
    }
}
