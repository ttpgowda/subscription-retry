package com.thewealthweb.crmbackend.user.service;

import com.thewealthweb.crmbackend.security.CustomUserDetails;
import com.thewealthweb.crmbackend.security.JwtTokenProvider;
import com.thewealthweb.crmbackend.user.dto.AuthResponse;
import com.thewealthweb.crmbackend.user.dto.LoginRequest;
import com.thewealthweb.crmbackend.user.dto.RegisterRequest;
import com.thewealthweb.crmbackend.user.entity.RefreshToken;
import com.thewealthweb.crmbackend.user.entity.Role;
import com.thewealthweb.crmbackend.user.entity.User;
import com.thewealthweb.crmbackend.user.helper.RoleServiceHelper;
import com.thewealthweb.crmbackend.user.repository.UserRepository;
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

    public AuthResponse authenticate(LoginRequest request) {
        // Let Spring Security do the checking
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String token = jwtTokenProvider.generateToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(request.getUsername()).getToken();
        return new AuthResponse(token, refreshToken);
    }

    public AuthResponse register(RegisterRequest request) {

        Set<Role> roles = roleServiceHelper.resolveRolesOrDefault(null);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .enabled(true)
                .roles(roles)
                .build();

        userRepository.save(user);

        UserDetails userDetails = new CustomUserDetails(user);
         String token = jwtTokenProvider.generateToken(userDetails);
         String refreshToken = refreshTokenService.createRefreshToken(request.getUsername()).getToken();
         return new AuthResponse(token, refreshToken);
    }

    public AuthResponse refreshAccessToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService
                .findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        String username = refreshToken.getUser().getUsername();
        UserDetails userDetails = (UserDetails) userRepository.findByUsername(username).orElseThrow();

        String newAccessToken = jwtTokenProvider.generateToken(userDetails);
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