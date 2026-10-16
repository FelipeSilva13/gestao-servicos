package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.domain.Tenant;
import com.felipe.gestao_servicos.domain.Usuario;
import com.felipe.gestao_servicos.dto.response.CadastroEmpresaResponse;
import com.felipe.gestao_servicos.dto.request.CadastroEmpresaRequest;
import com.felipe.gestao_servicos.mapper.CadastroEmpresaMapper;
import com.felipe.gestao_servicos.repository.TenantRepository;
import com.felipe.gestao_servicos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/** Caso de uso público para iniciar um tenant. */
@Service
public class CadastroEmpresaService {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CadastroEmpresaMapper cadastroEmpresaMapper;

    public CadastroEmpresaService(TenantRepository tenantRepository, UsuarioRepository usuarioRepository,
                                  PasswordEncoder passwordEncoder, CadastroEmpresaMapper cadastroEmpresaMapper) {
        this.tenantRepository = tenantRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.cadastroEmpresaMapper = cadastroEmpresaMapper;
    }

    @Transactional
    public CadastroEmpresaResponse cadastrar(CadastroEmpresaRequest request) {
        String nomeEmpresa = request.nomeEmpresa().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (tenantRepository.existsByNomeIgnoreCase(nomeEmpresa)) {
            throw new IllegalArgumentException("Já existe uma empresa com este nome.");
        }
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("E-mail já está em uso.");
        }

        Tenant tenant = tenantRepository.save(cadastroEmpresaMapper.toTenant(nomeEmpresa));
        String senhaCodificada = passwordEncoder.encode(request.senha());
        Usuario administrador = cadastroEmpresaMapper.toAdministrador(email, senhaCodificada, ROLE_ADMIN, tenant);
        administrador = usuarioRepository.save(administrador);

        return cadastroEmpresaMapper.toResponse(tenant, administrador);
    }
}
