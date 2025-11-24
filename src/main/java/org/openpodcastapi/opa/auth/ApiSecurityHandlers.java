package org.openpodcastapi.opa.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ApiSecurityHandlers {
    /// Returns an unauthorized response for unauthenticate API queries
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
