package com.thewealthweb.crmbackend.tenant.dto;

import lombok.Data;

@Data
public class TenantDTO {
    private String tenantId;
    private String name;
    private String contactEmail;
    private String phone;
    private boolean active = true;
}
