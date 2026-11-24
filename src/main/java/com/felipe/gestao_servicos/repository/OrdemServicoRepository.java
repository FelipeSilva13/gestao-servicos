package com.felipe.gestao_servicos.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.enums.StatusOS;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {
    List<OrdemServico> findAllByTenant_Id(Long tenantId);
    List<OrdemServico> findAllByTenant_IdOrderByDataCriacaoDesc(Long tenantId);
    List<OrdemServico> findTop3ByTenant_IdOrderByDataCriacaoDesc(Long tenantId);
    long countByTenant_Id(Long tenantId);
    long countByTenant_IdAndStatusNot(Long tenantId, StatusOS status);
    long countByTenant_IdAndStatusNotIn(Long tenantId, Collection<StatusOS> status);
    long countByTenant_IdAndStatus(Long tenantId, StatusOS status);
    Optional<OrdemServico> findByIdAndTenant_Id(Long id, Long tenantId);
    List<OrdemServico> findByTecnicoIdAndTenant_Id(Long tecnicoId, Long tenantId);
    List<OrdemServico> findByClienteIdAndTenant_Id(Long clienteId, Long tenantId);
    List<OrdemServico> findByStatusAndTenant_Id(StatusOS status, Long tenantId);

    // Portal do cliente: sempre filtrado pelo cliente dono da ordem
    List<OrdemServico> findByCliente_IdAndTenant_IdOrderByDataCriacaoDesc(Long clienteId, Long tenantId);
    Optional<OrdemServico> findByIdAndCliente_IdAndTenant_Id(Long id, Long clienteId, Long tenantId);

    // Área do técnico: sempre filtrado pelo técnico designado
    List<OrdemServico> findByTecnico_IdAndTenant_IdOrderByDataCriacaoDesc(Long tecnicoId, Long tenantId);
    Optional<OrdemServico> findByIdAndTecnico_IdAndTenant_Id(Long id, Long tecnicoId, Long tenantId);

    /** Ordens com horário marcado no período, ignorando as já encerradas (não ocupam a agenda). */
    List<OrdemServico> findByTenant_IdAndDataAgendadaBetweenAndStatusNotInOrderByDataAgendada(
            Long tenantId, LocalDateTime inicio, LocalDateTime fim, Collection<StatusOS> status);

    @Query("select o from OrdemServico o where o.tecnico.id = :tecnicoId and o.status = :status and o.tenant.id = :tenantId")
    List<OrdemServico> findByTecnicoAndStatus(@Param("tecnicoId") Long tecnicoId, @Param("status") StatusOS status, @Param("tenantId") Long tenantId);

    @Query("select o from OrdemServico o where o.cliente.id = :clienteId and o.status = :status and o.tenant.id = :tenantId")
    List<OrdemServico> findByClienteAndStatus(@Param("clienteId") Long clienteId, @Param("status") StatusOS status, @Param("tenantId") Long tenantId);
}
