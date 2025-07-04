package com.thewealthweb.srbackend.tenant.service;

import com.thewealthweb.srbackend.tenant.dto.TenantOnboardingRequest;
import com.thewealthweb.srbackend.tenant.entity.Tenant;
import com.thewealthweb.srbackend.tenant.repository.TenantRepository;
import com.thewealthweb.srbackend.user.entity.Role;
import com.thewealthweb.srbackend.user.entity.User;
import com.thewealthweb.srbackend.user.helper.RoleServiceHelper;
import com.thewealthweb.srbackend.user.repository.RoleRepository;
import com.thewealthweb.srbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections; // For creating singleton set
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor // Lombok for constructor injection
public class TenantOnboardingService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // Inject RoleRepository directly or via RoleServiceHelper
    private final PasswordEncoder passwordEncoder;
    private final RoleServiceHelper roleServiceHelper; // Use your existing helper

    @Transactional // Ensures atomicity: both tenant and user are created or none are
    public Tenant onboardNewTenant(TenantOnboardingRequest request) {
        // 1. Check for existing Tenant ID (prevent duplicates)
        if (tenantRepository.findByTenantId(request.getTenantId()).isPresent()) {
            throw new IllegalArgumentException("Tenant with ID '" + request.getTenantId() + "' already exists.");
        }

        // 2. Check for existing username (important for multi-tenant unique usernames)
        // This check should be global (bypassing tenant filter) or rely on a unique constraint.
        // Assuming your username column has a unique constraint, the save operation will fail.
        // If not, you might need a custom global query or ensure the unique constraint covers (username, tenant_id)
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken.");
        }

        // 3. Create Tenant
        Tenant newTenant = Tenant.builder()
                .tenantId(request.getTenantId())
                .name(request.getTenantName())
                .contactEmail(request.getContactEmail())
                .phone(request.getPhone())
                .subDomain(request.getSubDomain())
                .active(true) // Always active on creation
                .build();

        newTenant = tenantRepository.save(newTenant); // Save tenant to get its ID if needed, and persist

        // 4. Create Initial User (Company Admin) for this Tenant
        // Always assign the "COMPANY_ADMIN" role
        Role companyAdminRole = roleRepository.findByName("COMPANY_ADMIN")
                .orElseThrow(() -> new RuntimeException("COMPANY_ADMIN role not found in database. Please configure roles."));

        Set<Role> initialRoles = new HashSet<>(Collections.singletonList(companyAdminRole));

        User initialUser = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getUserEmail())
                .fullName(request.getFullName())
                .tenant(newTenant) // Link user directly to the newly created tenant
                .enabled(true)
                .roles(initialRoles)
                .build();

        userRepository.save(initialUser); // Save the initial user

        return newTenant; // Return the created tenant
    }

    // You might add other methods related to tenant lifecycle here later.
}