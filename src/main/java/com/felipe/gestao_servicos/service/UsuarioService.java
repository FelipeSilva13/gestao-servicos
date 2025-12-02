package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.domain.Usuario;
import com.felipe.gestao_servicos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public Usuario registrar(String email, String senha) {
        if (repo.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email já está em uso.");
        }

        Usuario u = new Usuario(
                email,
                encoder.encode(senha),
                "ROLE_USER"
        );

        return repo.save(u);
    }

    public Usuario buscarPorEmail(String email) {
        return repo.findByEmail(email).orElse(null);
    }

    public Usuario salvar(Usuario u) {
        if (u.getRole() == null) {
            u.setRole("ROLE_USER");
        }
        if (u.getSenha() != null && !u.getSenha().isBlank()) {
            if (!u.getSenha().startsWith("$2")) {
                u.setSenha(encoder.encode(u.getSenha()));
            }
        }
        return repo.save(u);
    }

    public List<Usuario> listar() { return repo.findAll(); }

    public Usuario obter(Long id) { return repo.findById(id).orElse(null); }

    public void excluir(Long id) { repo.deleteById(id); }
}

