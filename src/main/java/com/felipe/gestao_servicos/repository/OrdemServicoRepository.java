package com.felipe.gestao_servicos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.enums.StatusOS;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {
    List<OrdemServico> findAllByTenant_Id(Long tenantId);
    List<OrdemServico> findTop3ByTenant_IdOrderByDataCriacaoDesc(Long tenantId);
    long countByTenant_Id(Long tenantId);
    long countByTenant_IdAndStatusNot(Long tenantId, StatusOS status);
    Optional<OrdemServico> findByIdAndTenant_Id(Long id, Long tenantId);
    List<OrdemServico> findByTecnicoIdAndTenant_Id(Long tecnicoId, Long tenantId);
    List<OrdemServico> findByClienteIdAndTenant_Id(Long clienteId, Long tenantId);
    List<OrdemServico> findByStatusAndTenant_Id(StatusOS status, Long tenantId);

    @Query("select o from OrdemServico o where o.tecnico.id = :tecnicoId and o.status = :status and o.tenant.id = :tenantId")
    List<OrdemServico> findByTecnicoAndStatus(@Param("tecnicoId") Long tecnicoId, @Param("status") StatusOS status, @Param("tenantId") Long tenantId);

    @Query("select o from OrdemServico o where o.cliente.id = :clienteId and o.status = :status and o.tenant.id = :tenantId")
    List<OrdemServico> findByClienteAndStatus(@Param("clienteId") Long clienteId, @Param("status") StatusOS status, @Param("tenantId") Long tenantId);
}