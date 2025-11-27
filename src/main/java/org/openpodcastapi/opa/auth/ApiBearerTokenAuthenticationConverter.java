package org.openpodcastapi.opa.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

/// A converter that handles JWT-based auth for API requests.
///
/// This converter targets only the API endpoints at `/api`.
/// Auth for the frontend is handled by Spring's form login.
@Component
public class ApiBearerTokenAuthenticationConverter implements AuthenticationConverter {

    private static final Logger log = getLogger(ApiBearerTokenAuthenticationConverter.class);

    private final BearerTokenAuthenticationConverter delegate =
            new BearerTokenAuthenticationConverter();

    @Override
    public Authentication convert(HttpServletRequest request) {

        final var path = request.getRequestURI();

        // Don't authenticate the auth endpoints
        if (path.startsWith("/api/auth/")) {
            log.debug("Bypassing token check for auth endpoint");
            return null;
        }

        // If the request has no Bearer token, return null
        final var header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("Request with no auth header sent to {}", request.getRequestURI());
            return null;
        }

        log.debug("Converting request");
        // Task Spring Boot with handling the request
        return delegate.convert(request);
    }
}
