package com.thewealthweb.srbackend.tenant.repository;

import com.thewealthweb.srbackend.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByName(String name);
    Optional<Tenant> findByTenantId(String tenantId);

}