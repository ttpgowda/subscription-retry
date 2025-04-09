package com.thewealthweb.crmbackend.tenant.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Log auth failure here
        log.warn("Authentication failed for path: {}", request.getRequestURI());
        log.debug("Reason: {}", authException.getMessage());

        // Build JSON error response
        String jsonResponse = String.format("""
        {
            "timestamp": "%s",
            "status": %d,
            "error": "Unauthorized",
            "message": "%s",
            "path": "%s"
        }
        """,
                java.time.ZonedDateTime.now(),
                HttpServletResponse.SC_UNAUTHORIZED,
                authException.getMessage(),
                request.getRequestURI()
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);    }
}
