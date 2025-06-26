package com.thewealthweb.srbackend.user.service;

import com.thewealthweb.srbackend.security.CustomUserDetails;
import com.thewealthweb.srbackend.security.JwtTokenProvider;
import com.thewealthweb.srbackend.tenant.config.TenantContext;
import com.thewealthweb.srbackend.tenant.entity.Tenant;
import com.thewealthweb.srbackend.tenant.repository.TenantRepository;
import com.thewealthweb.srbackend.user.dto.AuthResponse;
import com.thewealthweb.srbackend.user.dto.LoginRequest;
import com.thewealthweb.srbackend.user.dto.RegisterRequest;
import com.thewealthweb.srbackend.user.entity.RefreshToken;
import com.thewealthweb.srbackend.user.entity.Role;
import com.thewealthweb.srbackend.user.entity.User;
import com.thewealthweb.srbackend.user.helper.RoleServiceHelper;
import com.thewealthweb.srbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RoleServiceHelper roleServiceHelper;
    private final RefreshTokenService refreshTokenService;
    private final TenantRepository tenantRepository;

    // In AuthService
    public AuthResponse authenticate(LoginRequest request) {
        // Let Spring Security do the checking
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        // Get the authenticated UserDetails
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Assuming CustomUserDetails provides access to your User entity
        // You must ensure CustomUserDetails has a method like getUser() that returns your User entity
        if (!(userDetails instanceof CustomUserDetails)) {
            throw new IllegalStateException("Principal is not an instance of CustomUserDetails");
        }
        User authenticatedUser = ((CustomUserDetails) userDetails).getUser();

        // Generate access token (ensure JwtTokenProvider includes tenant_id from authenticatedUser)
        String token = jwtTokenProvider.generateToken(userDetails); // This method should already use userDetails to get tenant_id

        // Create refresh token by passing the *authenticated User object*
        String refreshToken = refreshTokenService.createRefreshToken(authenticatedUser).getToken();

        return new AuthResponse(token, refreshToken);
    }

    public AuthResponse register(RegisterRequest request) {
        Set<Role> roles = roleServiceHelper.resolveRolesOrDefault(null); // Review default roles

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // --- CRITICAL CHANGE START ---
        String currentTenantIdStr = TenantContext.getTenantId();
        if (currentTenantIdStr == null || currentTenantIdStr.equals("default_tenant_id_from_your_config")) { // Replace with your actual default
            throw new RuntimeException("Tenant context not found for user registration. This endpoint is likely for existing tenants only.");
        }
        Tenant tenant = tenantRepository.findByTenantId(currentTenantIdStr)
                .orElseThrow(() -> new RuntimeException("Tenant not found for ID: " + currentTenantIdStr));
        // --- CRITICAL CHANGE END ---

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .enabled(true)
                .roles(roles)
                .tenant(tenant) // <-- Assign the tenant here!
                .build();

        userRepository.save(user);

        UserDetails userDetails = new CustomUserDetails(user); // Ensure CustomUserDetails wraps the User object
        String token = jwtTokenProvider.generateToken(userDetails); // This MUST include tenant_id in JWT claims
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken(); // Pass User object directly
        return new AuthResponse(token, refreshToken);
    }

    // In AuthService.refreshAccessToken
    public AuthResponse refreshAccessToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService
                .findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // String username = refreshToken.getUser().getUsername(); // This line is not strictly needed
        User user = refreshToken.getUser(); // Get the user directly from the refresh token

        // Ensure CustomUserDetails is used, providing access to the tenant
        UserDetails userDetails = new CustomUserDetails(user);

        String newAccessToken = jwtTokenProvider.generateToken(userDetails); // This token MUST include tenant_id
        return new AuthResponse(newAccessToken, refreshTokenStr);
    }

    public void logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        refreshTokenService.deleteByUser(user);
    }

    public void changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate all refresh tokens for this user
        refreshTokenService.deleteByUser(user);
    }
}