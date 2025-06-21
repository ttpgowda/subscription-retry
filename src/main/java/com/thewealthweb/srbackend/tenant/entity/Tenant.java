package com.thewealthweb.srbackend.tenant.entity;

import com.thewealthweb.srbackend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String tenantId;  // Used in @TenantId

    private String name;

    private String contactEmail;

    private String phone;

    private boolean active = true;
}
