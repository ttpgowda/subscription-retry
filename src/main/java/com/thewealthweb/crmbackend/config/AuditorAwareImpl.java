package com.thewealthweb.crmbackend.config;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Return the current username from security context
        //Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //return auth != null && auth.isAuthenticated() ? Optional.of(auth.getName()) : Optional.empty();
        return Optional.of("admin");
    }
}
