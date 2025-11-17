package com.felipe.gestao_servicos.repository;

import com.felipe.gestao_servicos.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}