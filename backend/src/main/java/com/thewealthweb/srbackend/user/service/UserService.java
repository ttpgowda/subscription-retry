package com.thewealthweb.srbackend.user.service;

import com.thewealthweb.srbackend.tenant.entity.Tenant;
import com.thewealthweb.srbackend.tenant.repository.TenantRepository;
import com.thewealthweb.srbackend.user.dto.UserDTO;
import com.thewealthweb.srbackend.user.entity.User;
import com.thewealthweb.srbackend.user.entity.Role;
import com.thewealthweb.srbackend.user.helper.RoleServiceHelper;
import com.thewealthweb.srbackend.user.mapper.UserMapper;
import com.thewealthweb.srbackend.user.repository.UserRepository;
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
    private final TenantRepository tenantRepository;
    private final UserMapper userMapper;

    public User createUser(UserDTO dto) {

        // 1. Fetch the Tenant entity using the logical tenant ID from the DTO
        //    We use findByTenantId, which you added to TenantRepository earlier.
        Tenant tenant = tenantRepository.findByTenantId(dto.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + dto.getTenantId()));

        Set<Role> roles = roleServiceHelper.resolveRolesOrDefault(dto.getRoles());

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .tenant(tenant)
                .enabled(dto.isEnabled())
                .roles(roles)
                .build();

        return userRepository.save(user);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users=  userRepository.findAll();

        return userMapper.toDtoList(users);
    }

    public UserDTO getUserById(Long id) {
        User user =  userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        return userMapper.toDto(user);
    }

    public UserDTO updateUser(Long id, UserDTO dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

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

        User savedUser = userRepository.save(existingUser);
        return userMapper.toDto(savedUser); // ✅ Map to DTO before returning
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        userRepository.delete(user);
    }
}

