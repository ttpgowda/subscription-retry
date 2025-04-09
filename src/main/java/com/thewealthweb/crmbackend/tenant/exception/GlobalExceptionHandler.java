package com.thewealthweb.crmbackend.tenant.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        System.err.println("PreAuthorize failed: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException ex) {
        System.err.println("Authentication error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
    }
}
