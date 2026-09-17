package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.dto.request.TecnicoRequest;
import com.felipe.gestao_servicos.dto.response.TecnicoResponse;
import com.felipe.gestao_servicos.service.TecnicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tecnicos")
public class TecnicoController {
    private final TecnicoService service;

    public TecnicoController(TecnicoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TecnicoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TecnicoResponse> buscarPorId(@PathVariable Long id) {
        TecnicoResponse tecnicoResponse = service.buscarPorId(id);
        return tecnicoResponse != null
                ? ResponseEntity.ok(tecnicoResponse)
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<TecnicoResponse> criar(@Valid @RequestBody TecnicoRequest tecnicoRequest) {
        return ResponseEntity.ok(service.salvar(tecnicoRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TecnicoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TecnicoRequest tecnicoRequest) {
        TecnicoResponse atualizado = service.atualizar(id, tecnicoRequest);
        return atualizado != null
                ? ResponseEntity.ok(atualizado)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}