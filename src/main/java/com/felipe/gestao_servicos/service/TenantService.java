package com.felipe.gestao_servicos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.felipe.gestao_servicos.domain.Tenant;
import com.felipe.gestao_servicos.repository.TenantRepository;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public Tenant createTenant(String nome) {
        Tenant tenant = new Tenant();
        tenant.setNome(nome);
        return tenantRepository.save(tenant);
    }

}
