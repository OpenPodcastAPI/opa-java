package org.openpodcastapi.opa.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/// Contains handlers for token-related errors on API endpoints
@Component
public class ApiSecurityHandlers {
    /// Returns an unauthorized response for unauthenticate API queries
    ///
    /// @return an unauthorized response with a JSON-formatted error message
    @Bean
    public AuthenticationEntryPoint apiAuthenticationEntryPoint() {
        return (_, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"error": "unauthorized", "message": "%s"}
                    """.formatted(authException.getMessage()));
        };
    }

    /// Returns a forbidden response for API queries
    ///
    /// @return a forbidden response with a JSON-formatted error message
    @Bean
    public AccessDeniedHandler apiAccessDeniedHandler() {
        return (_, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"error": "forbidden", "message": "%s"}
                    """.formatted(exception.getMessage()));
        };
    }
}
