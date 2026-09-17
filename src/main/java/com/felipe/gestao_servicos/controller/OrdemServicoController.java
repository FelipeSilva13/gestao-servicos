package com.felipe.gestao_servicos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.dto.request.OrdemRequest;
import com.felipe.gestao_servicos.dto.response.OrdemResponse;
import com.felipe.gestao_servicos.enums.StatusOS;
import com.felipe.gestao_servicos.service.OrdemServicoService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@Validated
@RequestMapping("/api/os")
public class OrdemServicoController {
    private final OrdemServicoService service;

    public OrdemServicoController(OrdemServicoService service) { this.service = service; }

    @GetMapping
    public List<OrdemResponse> listar() {

        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemServico> buscarPorId(@PathVariable @Positive Long id) {
        OrdemServico o = service.buscarPorId(id);
        return o != null ? ResponseEntity.ok(o) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<OrdemResponse> criar(@Valid @RequestBody OrdemRequest ordemServicoRequest) {
        return ResponseEntity.ok(service.salvar(ordemServicoRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdemResponse> atualizar(@PathVariable @Positive Long id, @Valid @RequestBody OrdemRequest ordemRequest) {
        OrdemServico existente = service.buscarPorId(id);
        if (existente == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(service.atualizar(id, ordemRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrdemServico> atualizarStatus(@PathVariable @Positive Long id, @RequestParam StatusOS status) {
        OrdemServico atualizado = service.atualizarStatus(id, status);
        return atualizado != null ? ResponseEntity.ok(atualizado) : ResponseEntity.notFound().build();
    }

    @GetMapping("/status/{status}")
    public List<OrdemResponse> listarPorStatus(@PathVariable StatusOS status) {

        return service.listarPorStatus(status);
    }

    @GetMapping("/tecnico/{id}")
    public List<OrdemResponse> listarPorTecnico(@PathVariable Long id) {

        return service.listarPorTecnico(id);
    }

    @GetMapping("/cliente/{id}")
    public List<OrdemResponse> listarPorCliente(@PathVariable Long id) {

        return service.listarPorCliente(id);
    }
}