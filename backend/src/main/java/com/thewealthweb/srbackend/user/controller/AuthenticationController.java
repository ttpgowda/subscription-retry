package com.thewealthweb.srbackend.user.controller;

import com.thewealthweb.srbackend.security.JwtTokenProvider;
import com.thewealthweb.srbackend.user.dto.LoginRequest;
import com.thewealthweb.srbackend.user.dto.RegisterRequest;
import com.thewealthweb.srbackend.user.repository.UserRepository;
import com.thewealthweb.srbackend.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> payload) {
        String refreshToken = payload.get("refreshToken");
        return ResponseEntity.ok(authService.refreshAccessToken(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String username) {
        authService.logout(username);
        return ResponseEntity.ok("Logged out successfully");
    }
    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestParam String username, @RequestParam String newPassword) {
        authService.changePassword(username, newPassword);
        return ResponseEntity.ok("Password changed successfully");
    }
}