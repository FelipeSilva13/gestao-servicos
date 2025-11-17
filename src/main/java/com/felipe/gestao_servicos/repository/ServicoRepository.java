package com.felipe.gestao_servicos.repository;

import com.felipe.gestao_servicos.domain.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
}