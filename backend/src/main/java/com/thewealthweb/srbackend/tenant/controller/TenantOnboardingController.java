package com.thewealthweb.srbackend.tenant.controller;

import com.thewealthweb.srbackend.tenant.dto.TenantOnboardingRequest;
import com.thewealthweb.srbackend.tenant.entity.Tenant;
import com.thewealthweb.srbackend.tenant.service.TenantOnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboard-tenant") // New dedicated endpoint
@RequiredArgsConstructor
public class TenantOnboardingController {

    private final TenantOnboardingService tenantOnboardingService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')") // Only SUPER_ADMIN can use this
    public ResponseEntity<Tenant> onboardNewTenant(@Valid @RequestBody TenantOnboardingRequest request) {
        Tenant newTenant = tenantOnboardingService.onboardNewTenant(request);
        // Returning the created tenant is useful for the client to confirm
        return new ResponseEntity<>(newTenant, HttpStatus.CREATED);
    }
}