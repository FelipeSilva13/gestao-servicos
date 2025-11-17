package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.dto.AtualizaStatusRequest;
import com.felipe.gestao_servicos.enums.StatusOS;
import com.felipe.gestao_servicos.service.OrdemServicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/os")
public class OrdemServicoController {
    private final OrdemServicoService service;

    public OrdemServicoController(OrdemServicoService service) { this.service = service; }

    @GetMapping
    public List<OrdemServico> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemServico> obter(@PathVariable Long id) {
        OrdemServico o = service.obter(id);
        return o != null ? ResponseEntity.ok(o) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<OrdemServico> criar(@Valid @RequestBody OrdemServico o) {
        return ResponseEntity.ok(service.salvar(o));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdemServico> atualizar(@PathVariable Long id, @Valid @RequestBody OrdemServico o) {
        OrdemServico existente = service.obter(id);
        if (existente == null) return ResponseEntity.notFound().build();
        o.setId(id);
        return ResponseEntity.ok(service.salvar(o));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrdemServico> atualizarStatus(@PathVariable Long id, @RequestBody AtualizaStatusRequest req) {
        OrdemServico atualizado = service.atualizarStatus(id, req.status());
        return atualizado != null ? ResponseEntity.ok(atualizado) : ResponseEntity.notFound().build();
    }

    @GetMapping("/status/{status}")
    public List<OrdemServico> listarPorStatus(@PathVariable StatusOS status) { return service.listarPorStatus(status); }

    @GetMapping("/tecnico/{id}")
    public List<OrdemServico> listarPorTecnico(@PathVariable Long id) { return service.listarPorTecnico(id); }

    @GetMapping("/cliente/{id}")
    public List<OrdemServico> listarPorCliente(@PathVariable Long id) { return service.listarPorCliente(id); }
}