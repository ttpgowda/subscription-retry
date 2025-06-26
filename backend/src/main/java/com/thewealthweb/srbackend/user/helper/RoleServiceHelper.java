package com.thewealthweb.srbackend.user.helper;

import com.thewealthweb.srbackend.user.entity.Role;
import com.thewealthweb.srbackend.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RoleServiceHelper {

    private final RoleRepository roleRepository;

    public Set<Role> resolveRolesOrDefault(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        if (roleNames == null || roleNames.isEmpty()) {
            roles.add(roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Default USER role not found")));
            return roles;
        }

        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            roles.add(role);
        }

        return roles;
    }
}