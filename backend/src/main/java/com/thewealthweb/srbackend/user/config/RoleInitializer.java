package com.thewealthweb.srbackend.user.config;

import com.thewealthweb.srbackend.user.entity.Role;
import com.thewealthweb.srbackend.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        List<String> rolesToCheck = List.of(
                "SUPER_ADMIN",     // Access to everything
                "COMPANY_ADMIN",   // Manages users & tenants under their company
                "MANAGER",         // Manages teams/projects
                "SUPPORT",         // Customer support level access
                "USER"             // Basic authenticated user
        );

        for (String roleName : rolesToCheck) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                Role role = Role.builder().name(roleName).build();
                return roleRepository.save(role);
            });
        }
    }
}
