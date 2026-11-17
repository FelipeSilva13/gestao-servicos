package com.felipe.gestao_servicos.repository;

import com.felipe.gestao_servicos.domain.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnexoRepository extends JpaRepository<Anexo, Long> {
    List<Anexo> findByOrdemIdAndTenantIdOrderByDataEnvioAsc(Long ordemId, Long tenantId);
    Optional<Anexo> findByIdAndTenantId(Long id, Long tenantId);
}
