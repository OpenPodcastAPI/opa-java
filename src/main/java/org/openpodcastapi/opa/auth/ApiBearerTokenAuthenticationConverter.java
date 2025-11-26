package org.openpodcastapi.opa.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;

/// A converter that handles JWT-based auth for API requests.
///
/// This converter targets only the API endpoints at `/api`.
/// Auth for the frontend is handled by Spring's form login.
@Component
public class ApiBearerTokenAuthenticationConverter implements AuthenticationConverter {

    private final BearerTokenAuthenticationConverter delegate =
            new BearerTokenAuthenticationConverter();

    @Override
    public Authentication convert(HttpServletRequest request) {

        final var path = request.getRequestURI();

        // Don't authenticate the auth endpoints
        if (path.startsWith("/api/auth/")) {
            return null;
        }

        // If the request has no Bearer token, return null
        final var header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }

        // Task Spring Boot with handling the request
        return delegate.convert(request);
    }
}
