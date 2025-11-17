package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.domain.Servico;
import com.felipe.gestao_servicos.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
public class ServicoController {
    private final ServicoService service;

    public ServicoController(ServicoService service) { this.service = service; }

    @GetMapping
    public List<Servico> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public ResponseEntity<Servico> obter(@PathVariable Long id) {
        Servico s = service.obter(id);
        return s != null ? ResponseEntity.ok(s) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Servico> criar(@Valid @RequestBody Servico s) {
        return ResponseEntity.ok(service.salvar(s));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Servico> atualizar(@PathVariable Long id, @Valid @RequestBody Servico s) {
        Servico existente = service.obter(id);
        if (existente == null) return ResponseEntity.notFound().build();
        s.setId(id);
        return ResponseEntity.ok(service.salvar(s));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}