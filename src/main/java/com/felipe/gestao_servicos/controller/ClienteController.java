package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.domain.Cliente;
import com.felipe.gestao_servicos.dto.request.ClienteRequest;
import com.felipe.gestao_servicos.dto.response.ClienteResponse;
import com.felipe.gestao_servicos.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service; }

    @GetMapping
    public List<ClienteResponse> listar() {
        return service.listar(); }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
        ClienteResponse cliente = service.buscarPorId(id);
        return cliente != null
                ? ResponseEntity.ok(cliente)
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(
            @Valid @RequestBody ClienteRequest clienteRequest) {
        return ResponseEntity.ok(service.salvar(clienteRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest clienteRequest) {

        ClienteResponse atualizado = service.atualizar(id, clienteRequest);

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