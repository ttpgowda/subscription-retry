package com.thewealthweb.srbackend.stripe.repository;

import com.thewealthweb.srbackend.stripe.entity.StripeAccountConnection;
import com.thewealthweb.srbackend.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StripeAccountConnectionRepository extends JpaRepository<StripeAccountConnection, Long> {
    Optional<StripeAccountConnection> findByTenant(Tenant tenant);
    Optional<StripeAccountConnection> findByStripeUserId(String stripeUserId);
}