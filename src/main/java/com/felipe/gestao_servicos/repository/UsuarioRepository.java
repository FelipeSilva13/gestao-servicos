package com.felipe.gestao_servicos.repository;


import com.felipe.gestao_servicos.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    /** Usuários da equipe da empresa (exclui acessos do portal do cliente). */
    List<Usuario> findByTenant_IdAndRoleNot(Long tenantId, String role);

    List<Usuario> findByCliente_IdAndTenant_Id(Long clienteId, Long tenantId);
}
