package com.felipe.gestao_servicos.repository;

import com.felipe.gestao_servicos.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findAllByTenant_Id(Long tenant_id);

    long countByTenant_Id(Long tenantId);

    Optional<Cliente> findByIdAndTenant_Id(Long id, Long tenant_id);

    Long tenant_Id(Long tenantId);
}