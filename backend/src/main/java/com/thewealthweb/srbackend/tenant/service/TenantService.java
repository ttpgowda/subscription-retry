package com.thewealthweb.srbackend.tenant.service;

import com.thewealthweb.srbackend.tenant.dto.TenantDTO;
import com.thewealthweb.srbackend.tenant.entity.Tenant;
import com.thewealthweb.srbackend.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    public Tenant createTenant(TenantDTO dto) {
        Tenant tenant = Tenant.builder()
                .tenantId(dto.getTenantId())
                .name(dto.getName())
                .contactEmail(dto.getContactEmail())
                .phone(dto.getPhone())
                .active(dto.isActive())
                .build();

        return tenantRepository.save(tenant);
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Tenant getTenantById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + id));
    }

    public Tenant updateTenant(Long id, TenantDTO dto) {
        Tenant existing = getTenantById(id);
        existing.setTenantId(dto.getTenantId());
        existing.setName(dto.getName());
        existing.setContactEmail(dto.getContactEmail());
        existing.setPhone(dto.getPhone());
        existing.setActive(dto.isActive());

        return tenantRepository.save(existing);
    }

    public void deleteTenant(Long id) {
        tenantRepository.deleteById(id);
    }
}
