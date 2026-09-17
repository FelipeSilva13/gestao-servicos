package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.dto.request.ServicoRequest;
import com.felipe.gestao_servicos.dto.response.ServicoResponse;
import com.felipe.gestao_servicos.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
public class ServicoController {
    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service; }

    @GetMapping
    public List<ServicoResponse> listar() {
        return service.listar(); }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponse> buscarProId(@PathVariable Long id) {
        ServicoResponse s = service.buscarPorId(id);
        return s != null
                ? ResponseEntity.ok(s)
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ServicoResponse> cria(
            @Valid @RequestBody ServicoRequest servicoRequest) {
        return ResponseEntity.ok(service.salvar(servicoRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ServicoRequest dto) {

        ServicoResponse atualizado = service.atualizar(id, dto);

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