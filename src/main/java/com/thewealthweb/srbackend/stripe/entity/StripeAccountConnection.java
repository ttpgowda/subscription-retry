package com.thewealthweb.srbackend.stripe.entity;

import com.thewealthweb.srbackend.common.entity.BaseEntity; // Assuming BaseEntity has id, createdDate, updatedDate
import com.thewealthweb.srbackend.tenant.entity.Tenant; // Import your Tenant entity
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "stripe_account_connections")
@Data
@EqualsAndHashCode(callSuper = true)
public class StripeAccountConnection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant; // Link to your Tenant entity

    @Column(name = "stripe_user_id", unique = true, nullable = false)
    private String stripeUserId; // The connected account ID (acct_...)

    // This token MUST be encrypted at rest.
    // For simplicity, it's a String here, but implement encryption.
    @Column(name = "access_token", nullable = false, length = 1024) // Increased length for encrypted token
    private String accessToken;

    @Column(name = "stripe_publishable_key")
    private String stripePublishableKey; // The publishable key for the connected account

    // You might add more fields as needed, e.g.,
    // private String scope;
    // private Long expiresAt;
    // private String refreshToken; // If using refresh tokens (standard Connect usually doesn't)

    // Consider adding audit fields if BaseEntity doesn't cover them.
}