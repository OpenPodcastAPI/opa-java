package org.openpodcastapi.opa.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;

@Component
public class ApiBearerTokenAuthenticationConverter implements AuthenticationConverter {

    private final BearerTokenAuthenticationConverter delegate =
            new BearerTokenAuthenticationConverter();

    @Override
    public Authentication convert(HttpServletRequest request) {

        String path = request.getRequestURI();

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
