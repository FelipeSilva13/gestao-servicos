package com.felipe.gestao_servicos.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.felipe.gestao_servicos.domain.Tecnico;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
	List<Tecnico> findAllByTenant_Id(Long tenantId);
	long countByTenant_Id(Long tenantId);
	Optional<Tecnico> findByIdAndTenant_Id(Long id, Long tenantId);
}