package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.domain.Cliente;
import com.felipe.gestao_servicos.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) { this.service = service; }

    @GetMapping
    public List<Cliente> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obter(@PathVariable Long id) {
        Cliente c = service.obter(id);
        return c != null ? ResponseEntity.ok(c) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Cliente> criar(@Valid @RequestBody Cliente c) {
        return ResponseEntity.ok(service.salvar(c));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizar(@PathVariable Long id, @Valid @RequestBody Cliente c) {
        Cliente existente = service.obter(id);
        if (existente == null) return ResponseEntity.notFound().build();
        c.setId(id);
        return ResponseEntity.ok(service.salvar(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}