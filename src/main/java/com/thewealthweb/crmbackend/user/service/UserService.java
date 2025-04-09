package com.thewealthweb.crmbackend.user.service;


import com.thewealthweb.crmbackend.user.dto.UserDTO;
import com.thewealthweb.crmbackend.user.entity.User;
import com.thewealthweb.crmbackend.user.entity.Role;
import com.thewealthweb.crmbackend.user.helper.RoleServiceHelper;
import com.thewealthweb.crmbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleServiceHelper roleServiceHelper;
    private final PasswordEncoder passwordEncoder;

    public User createUser(UserDTO dto) {
        Set<Role> roles = roleServiceHelper.resolveRolesOrDefault(dto.getRoles());

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .tenantId(dto.getTenantId())
                .enabled(dto.isEnabled())
                .roles(roles)
                .build();

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User updateUser(Long id, UserDTO dto) {
        User existingUser = getUserById(id);
        existingUser.setUsername(dto.getUsername());
        existingUser.setEmail(dto.getEmail());
        existingUser.setFullName(dto.getFullName());
        existingUser.setEnabled(dto.isEnabled());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            Set<Role> roles = roleServiceHelper.resolveRolesOrDefault(dto.getRoles());
            existingUser.setRoles(roles);
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}

