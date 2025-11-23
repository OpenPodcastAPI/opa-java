package org.openpodcastapi.opa.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        // If the user doesn't have access to the resource in question, return a 403
        response.setStatus(HttpStatus.FORBIDDEN.value());

        // Set content type to JSON
        response.setContentType("application/json");

        final var message = new AuthDTO.ErrorMessageDTO("Forbidden", "You do not have permission to access this resource");

        response.getWriter().write(objectMapper.writeValueAsString(message));
    }
}
