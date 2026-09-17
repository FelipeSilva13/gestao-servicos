package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantContext;
import com.felipe.gestao_servicos.domain.Tenant;
import com.felipe.gestao_servicos.domain.Usuario;
import com.felipe.gestao_servicos.dto.request.UsuarioRequest;
import com.felipe.gestao_servicos.dto.response.UsuarioResponse;
import com.felipe.gestao_servicos.repository.UsuarioRepository;
import com.felipe.gestao_servicos.repository.TenantRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;
    private final TenantRepository tenantRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder encoder,
            TenantRepository tenantRepository) {

        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
        this.tenantRepository = tenantRepository;
    }

    public UsuarioResponse salvar(UsuarioRequest dto) {

        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new RuntimeException("Email já está em uso.");
        }

        Usuario usuario = new Usuario();

        usuario.setEmail(dto.email());
        usuario.setSenha(encoder.encode(dto.senha()));
        usuario.setRole("ROLE_USER");
        usuario.setTenant(tenantAtualOuNovo(dto.email()));

        Usuario salvo = usuarioRepository.save(usuario);

        return toResponse(salvo);
    }

    public List<UsuarioResponse> listar() {

        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UsuarioResponse buscarPorId(Long id) {

        return usuarioRepository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    public UsuarioResponse buscarPorEmail(String email) {

        return usuarioRepository.findByEmail(email)
                .map(this::toResponse)
                .orElse(null);
    }

    public UsuarioResponse atualizar(
            Long id,
            UsuarioRequest dto) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElse(null);

        if (usuario == null) {
            return null;
        }

        usuario.setEmail(dto.email());

        if (dto.senha() != null && !dto.senha().isBlank()) {
            usuario.setSenha(
                    encoder.encode(dto.senha())
            );
        }

        Usuario atualizado = usuarioRepository.save(usuario);

        return toResponse(atualizado);
    }

    public void excluir(Long id) {
        usuarioRepository.deleteById(id);
    }

    private Tenant tenantAtualOuNovo(String email) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new IllegalStateException("Tenant não encontrado"));
        }
        Tenant tenant = new Tenant();
        tenant.setNome(email);
        return tenantRepository.save(tenant);
    }

    private UsuarioResponse toResponse(Usuario usuario) {

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRole()
        );
    }
}