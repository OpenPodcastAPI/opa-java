package org.openpodcastapi.opa.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.openpodcastapi.opa.util.JSONFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        // If the user doesn't have access to the resource in question, return a 403
        response.setStatus(HttpStatus.FORBIDDEN.value());

        // Set content type to JSON
        response.setContentType("application/json");

        AuthDTO.ErrorMessageDTO message = new AuthDTO.ErrorMessageDTO("Forbidden", "You do not have permission to access this resource");

        response.getWriter().write(JSONFormatter.parseDataToJSON(message));
    }
}
