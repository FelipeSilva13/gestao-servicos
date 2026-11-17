package com.felipe.gestao_servicos.repository;

import com.felipe.gestao_servicos.domain.OrdemHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdemHistoricoRepository extends JpaRepository<OrdemHistorico, Long> {
    List<OrdemHistorico> findByOrdemIdAndTenantIdOrderByDataAscIdAsc(Long ordemId, Long tenantId);
    long countByOrdemIdAndTenantId(Long ordemId, Long tenantId);
}
