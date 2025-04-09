package com.thewealthweb.crmbackend.tenant.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        // Log access denial here
        log.warn("Authentication failed for path: {}", request.getRequestURI());
        log.debug("Reason: {}", accessDeniedException.getMessage());

        String jsonResponse = String.format("""
        {
            "timestamp": "%s",
            "status": %d,
            "error": "Access Denied",
            "message": "%s",
            "path": "%s"
        }
        """,
                java.time.ZonedDateTime.now(),
                HttpServletResponse.SC_FORBIDDEN,
                accessDeniedException.getMessage(),
                request.getRequestURI()
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }
}
