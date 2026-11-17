package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantContext;
import com.felipe.gestao_servicos.domain.Cliente;
import com.felipe.gestao_servicos.domain.Usuario;
import com.felipe.gestao_servicos.repository.ClienteRepository;
import com.felipe.gestao_servicos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** A empresa libera (ou redefine) o acesso de um cliente ao portal. */
@Service
public class AcessoPortalService {

    public static final int SENHA_MINIMA = 8;

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AcessoPortalService(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository,
                               PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Cliente cliente(Long clienteId) {
        return clienteRepository.findByIdAndTenant_Id(clienteId, tenantId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }

    public Optional<Usuario> acessoExistente(Long clienteId) {
        return usuarioRepository.findByCliente_IdAndTenant_Id(clienteId, tenantId()).stream().findFirst();
    }

    /** Cria o usuário do portal com o e-mail do cliente, ou redefine a senha se já existir. */
    @Transactional
    public Usuario definirSenha(Long clienteId, String senha) {
        if (senha == null || senha.length() < SENHA_MINIMA) {
            throw new RegraNegocioException("A senha deve ter pelo menos " + SENHA_MINIMA + " caracteres");
        }
        Cliente cliente = cliente(clienteId);
        if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            throw new RegraNegocioException("Cadastre um e-mail para o cliente antes de liberar o acesso");
        }

        Usuario usuario = acessoExistente(clienteId).orElseGet(() -> {
            usuarioRepository.findByEmail(cliente.getEmail()).ifPresent(outro -> {
                throw new RegraNegocioException("O e-mail " + cliente.getEmail() + " já é usado por outro usuário do sistema");
            });
            Usuario novo = new Usuario();
            novo.setEmail(cliente.getEmail());
            novo.setRole(UsuarioAtual.ROLE_CLIENTE);
            novo.setCliente(cliente);
            novo.setTenant(cliente.getTenant());
            return novo;
        });
        // Mantém o login sincronizado se o e-mail do cliente mudou
        if (!usuario.getEmail().equalsIgnoreCase(cliente.getEmail())) {
            usuarioRepository.findByEmail(cliente.getEmail()).ifPresent(outro -> {
                throw new RegraNegocioException("O e-mail " + cliente.getEmail() + " já é usado por outro usuário do sistema");
            });
            usuario.setEmail(cliente.getEmail());
        }
        usuario.setSenha(passwordEncoder.encode(senha));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void revogar(Long clienteId) {
        cliente(clienteId);
        usuarioRepository.deleteAll(usuarioRepository.findByCliente_IdAndTenant_Id(clienteId, tenantId()));
    }

    private static Long tenantId() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant atual não configurado");
        }
        return tenantId;
    }
}
