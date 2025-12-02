package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.domain.Usuario;
import com.felipe.gestao_servicos.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) { this.service = service; }

    @GetMapping
    public List<Usuario> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obter(@PathVariable Long id) {
        Usuario u = service.obter(id);
        return u != null ? ResponseEntity.ok(u) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Usuario> criar(@Valid @RequestBody Usuario u) {
        return ResponseEntity.ok(service.salvar(u));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar(@PathVariable Long id, @Valid @RequestBody Usuario u) {
        Usuario existente = service.obter(id);
        if (existente == null) return ResponseEntity.notFound().build();
        u.setId(id);
        return ResponseEntity.ok(service.salvar(u));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
