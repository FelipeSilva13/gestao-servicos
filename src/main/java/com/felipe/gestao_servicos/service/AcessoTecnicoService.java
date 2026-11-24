package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantContext;
import com.felipe.gestao_servicos.domain.Tecnico;
import com.felipe.gestao_servicos.domain.Usuario;
import com.felipe.gestao_servicos.repository.TecnicoRepository;
import com.felipe.gestao_servicos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * O administrador libera (ou redefine/revoga) o acesso de um técnico à área do técnico.
 * As rotas que usam este serviço são restritas a ROLE_ADMIN no SecurityConfig.
 */
@Service
public class AcessoTecnicoService {

    public static final int SENHA_MINIMA = AcessoPortalService.SENHA_MINIMA;

    private final TecnicoRepository tecnicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AcessoTecnicoService(TecnicoRepository tecnicoRepository, UsuarioRepository usuarioRepository,
                                PasswordEncoder passwordEncoder) {
        this.tecnicoRepository = tecnicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Tecnico tecnico(Long tecnicoId) {
        return tecnicoRepository.findByIdAndTenant_Id(tecnicoId, tenantId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Técnico não encontrado"));
    }

    public Optional<Usuario> acessoExistente(Long tecnicoId) {
        return usuarioRepository.findByTecnico_IdAndTenant_Id(tecnicoId, tenantId()).stream().findFirst();
    }

    /** Cria o usuário do técnico com o e-mail dele, ou redefine a senha se já existir. */
    @Transactional
    public Usuario definirSenha(Long tecnicoId, String senha) {
        if (senha == null || senha.length() < SENHA_MINIMA) {
            throw new RegraNegocioException("A senha deve ter pelo menos " + SENHA_MINIMA + " caracteres");
        }
        Tecnico tecnico = tecnico(tecnicoId);
        if (tecnico.getEmail() == null || tecnico.getEmail().isBlank()) {
            throw new RegraNegocioException("Cadastre um e-mail para o técnico antes de liberar o acesso");
        }

        Usuario usuario = acessoExistente(tecnicoId).orElseGet(() -> {
            garantirEmailLivre(tecnico.getEmail());
            Usuario novo = new Usuario();
            novo.setEmail(tecnico.getEmail());
            novo.setRole(UsuarioAtual.ROLE_TECNICO);
            novo.setTecnico(tecnico);
            novo.setTenant(tecnico.getTenant());
            return novo;
        });
        // Mantém o login sincronizado se o e-mail do técnico mudou
        if (!usuario.getEmail().equalsIgnoreCase(tecnico.getEmail())) {
            garantirEmailLivre(tecnico.getEmail());
            usuario.setEmail(tecnico.getEmail());
        }
        usuario.setSenha(passwordEncoder.encode(senha));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void revogar(Long tecnicoId) {
        tecnico(tecnicoId);
        usuarioRepository.deleteAll(usuarioRepository.findByTecnico_IdAndTenant_Id(tecnicoId, tenantId()));
    }

    private void garantirEmailLivre(String email) {
        usuarioRepository.findByEmail(email).ifPresent(outro -> {
            throw new RegraNegocioException("O e-mail " + email + " já é usado por outro usuário do sistema");
        });
    }

    private static Long tenantId() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant atual não configurado");
        }
        return tenantId;
    }
}
