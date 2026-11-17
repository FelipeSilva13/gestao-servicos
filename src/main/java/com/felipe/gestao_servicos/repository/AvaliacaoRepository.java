package com.felipe.gestao_servicos.repository;

import com.felipe.gestao_servicos.domain.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    Optional<Avaliacao> findByOrdemIdAndTenantId(Long ordemId, Long tenantId);
    boolean existsByOrdemIdAndTenantId(Long ordemId, Long tenantId);
    List<Avaliacao> findByTenantIdAndOrdemIdIn(Long tenantId, List<Long> ordemIds);

    /** Linhas [tecnicoId, média da nota do técnico, quantidade de avaliações]. */
    @Query("""
            select o.tecnico.id, avg(a.notaTecnico), count(a)
            from Avaliacao a, OrdemServico o
            where a.ordemId = o.id and a.tenantId = :tenantId
              and o.tecnico is not null and a.notaTecnico is not null
            group by o.tecnico.id
            """)
    List<Object[]> mediasPorTecnico(@Param("tenantId") Long tenantId);
}
