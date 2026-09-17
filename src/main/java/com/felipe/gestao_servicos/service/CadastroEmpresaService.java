package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.domain.Tenant;
import com.felipe.gestao_servicos.domain.Usuario;
import com.felipe.gestao_servicos.dto.request.CadastroEmpresaRequest;
import com.felipe.gestao_servicos.repository.TenantRepository;
import com.felipe.gestao_servicos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Caso de uso público para iniciar um tenant. */
@Service
public class CadastroEmpresaService {
    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastroEmpresaService(TenantRepository tenantRepository, UsuarioRepository usuarioRepository,
                                  PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void cadastrar(CadastroEmpresaRequest request) {
        String nomeEmpresa = request.nomeEmpresa().trim();
        String email = request.email().trim().toLowerCase();
        if (tenantRepository.existsByNomeIgnoreCase(nomeEmpresa)) {
            throw new IllegalArgumentException("Já existe uma empresa com este nome.");
        }
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("E-mail já está em uso.");
        }

        Tenant tenant = new Tenant();
        tenant.setNome(nomeEmpresa);
        tenant = tenantRepository.save(tenant);

        Usuario administrador = new Usuario();
        administrador.setEmail(email);
        administrador.setSenha(passwordEncoder.encode(request.senha()));
        administrador.setRole("ROLE_ADMIN");
        administrador.setTenant(tenant);
        usuarioRepository.save(administrador);
    }
}
