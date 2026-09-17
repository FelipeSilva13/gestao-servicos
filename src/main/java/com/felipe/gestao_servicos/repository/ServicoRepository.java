package com.felipe.gestao_servicos.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.felipe.gestao_servicos.domain.Servico;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
	List<Servico> findAllByTenant_Id(Long tenantId);
	long countByTenant_Id(Long tenantId);
	Optional<Servico> findByIdAndTenant_Id(Long id, Long tenantId);
}