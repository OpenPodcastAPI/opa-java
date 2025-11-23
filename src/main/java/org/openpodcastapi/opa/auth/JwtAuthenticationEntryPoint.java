package org.openpodcastapi.opa.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    /// Returns a 401 when a request is made without a valid bearer token
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // If the request is being made without a valid bearer token, return a 401.
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        // Set content type to JSON
        response.setContentType("application/json");

        AuthDTO.ErrorMessageDTO message = new AuthDTO.ErrorMessageDTO("Access denied", "You need to log in to access this resource");

        response.getWriter().write(objectMapper.writeValueAsString(message));
    }
}
