package com.felipe.gestao_servicos.repository;


import com.felipe.gestao_servicos.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    /** Usuários da equipe da empresa (exclui acessos do portal do cliente). */
    List<Usuario> findByTenant_IdAndRoleNot(Long tenantId, String role);

    /** Usuários com um dos papéis informados (ex.: só a equipe interna, sem cliente nem técnico). */
    List<Usuario> findByTenant_IdAndRoleIn(Long tenantId, Collection<String> roles);

    List<Usuario> findByCliente_IdAndTenant_Id(Long clienteId, Long tenantId);

    List<Usuario> findByTecnico_IdAndTenant_Id(Long tecnicoId, Long tenantId);
}
