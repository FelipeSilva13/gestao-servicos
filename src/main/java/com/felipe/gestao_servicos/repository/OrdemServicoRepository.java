package com.felipe.gestao_servicos.repository;

import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.enums.StatusOS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {
    List<OrdemServico> findByTecnicoId(Long tecnicoId);
    List<OrdemServico> findByClienteId(Long clienteId);
    List<OrdemServico> findByStatus(StatusOS status);

    @Query("select o from OrdemServico o where o.tecnico.id = :tecnicoId and o.status = :status")
    List<OrdemServico> findByTecnicoAndStatus(@Param("tecnicoId") Long tecnicoId, @Param("status") StatusOS status);

    @Query("select o from OrdemServico o where o.cliente.id = :clienteId and o.status = :status")
    List<OrdemServico> findByClienteAndStatus(@Param("clienteId") Long clienteId, @Param("status") StatusOS status);
}