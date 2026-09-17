package com.felipe.gestao_servicos.repository;

import com.felipe.gestao_servicos.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    boolean existsByNomeIgnoreCase(String nome);
}
