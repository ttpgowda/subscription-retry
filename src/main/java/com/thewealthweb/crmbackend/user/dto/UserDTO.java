package com.thewealthweb.crmbackend.user.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UserDTO {
    private String username;
    private String password;
    private String email;
    private String fullName;
    private boolean enabled = true;
    private Set<String> roles;
    private String tenantId;
}