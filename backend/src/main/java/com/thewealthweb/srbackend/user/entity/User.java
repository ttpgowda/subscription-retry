package com.thewealthweb.srbackend.user.entity;

import com.thewealthweb.srbackend.common.entity.BaseEntity;
import com.thewealthweb.srbackend.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// Define the filter here. The 'condition' refers to properties of the entity.
// 'tenant.tenantId' navigates through the 'tenant' ManyToOne relationship to its 'tenantId' property.
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantIdentifier", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant.tenantId = :tenantIdentifier") // Apply the filter condition
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Many Users to One Tenant
    @JoinColumn(name = "tenant_id", nullable = false) // This creates the foreign key column named 'tenant_id'
//    @TenantId
    private Tenant tenant;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;

    private String fullName;

    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}