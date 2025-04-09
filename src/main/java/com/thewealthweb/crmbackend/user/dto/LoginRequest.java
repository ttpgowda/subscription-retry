package com.thewealthweb.crmbackend.user.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}