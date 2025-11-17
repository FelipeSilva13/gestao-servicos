package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.domain.Tecnico;
import com.felipe.gestao_servicos.service.TecnicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tecnicos")
public class TecnicoController {
    private final TecnicoService service;

    public TecnicoController(TecnicoService service) { this.service = service; }

    @GetMapping
    public List<Tecnico> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public ResponseEntity<Tecnico> obter(@PathVariable Long id) {
        Tecnico t = service.obter(id);
        return t != null ? ResponseEntity.ok(t) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Tecnico> criar(@Valid @RequestBody Tecnico t) {
        return ResponseEntity.ok(service.salvar(t));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tecnico> atualizar(@PathVariable Long id, @Valid @RequestBody Tecnico t) {
        Tecnico existente = service.obter(id);
        if (existente == null) return ResponseEntity.notFound().build();
        t.setId(id);
        return ResponseEntity.ok(service.salvar(t));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}