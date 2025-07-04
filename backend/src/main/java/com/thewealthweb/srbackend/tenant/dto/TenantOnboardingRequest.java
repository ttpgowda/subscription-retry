package com.thewealthweb.srbackend.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // Lombok annotation for getters, setters, toString, equals, hashCode
public class TenantOnboardingRequest {

    // --- Tenant Details ---
    @NotBlank(message = "Tenant ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Tenant ID can only contain letters, numbers, hyphens, and underscores.")
    @Size(min = 3, max = 50, message = "Tenant ID must be between 3 and 50 characters")
    private String tenantId; // e.g., "acme-corp", "global-solutions"

    @NotBlank(message = "Tenant name is required")
    @Size(min = 2, max = 100, message = "Tenant name must be between 2 and 100 characters")
    private String tenantName; // e.g., "ACME Corporation"

    @NotBlank(message = "Tenant contact email is required")
    @Email(message = "Invalid tenant contact email format")
    private String contactEmail;

    private String phone; // Optional
    private String subDomain; // Optional, e.g., "acme" for acme.your-app.com

    // --- Initial User (Company Admin) Details ---
    @NotBlank(message = "Initial user username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Initial user password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long") // Increased for better security
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character.")
    private String password;

    @NotBlank(message = "Initial user email is required")
    @Email(message = "Invalid initial user email format")
    private String userEmail;

    @NotBlank(message = "Initial user full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    // Note: No roles field here for the initial user, as it's always COMPANY_ADMIN
    // No 'enabled' field for tenant, default to true.
}