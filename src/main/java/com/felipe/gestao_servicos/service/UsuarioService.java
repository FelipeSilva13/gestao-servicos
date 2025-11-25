package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.domain.Usuario;
import com.felipe.gestao_servicos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
}

