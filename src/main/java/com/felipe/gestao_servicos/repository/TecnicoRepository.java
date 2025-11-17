package com.felipe.gestao_servicos.repository;

import com.felipe.gestao_servicos.domain.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
}