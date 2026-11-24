package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantContext;
import com.felipe.gestao_servicos.domain.Cliente;
import com.felipe.gestao_servicos.domain.Tenant;
import com.felipe.gestao_servicos.dto.request.ClienteRequest;
import com.felipe.gestao_servicos.dto.response.ClienteResponse;
import com.felipe.gestao_servicos.repository.ClienteRepository;
import com.felipe.gestao_servicos.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {
    private final ClienteRepository repository;
    private final TenantRepository tenantRepository;
    public ClienteService(ClienteRepository repository, TenantRepository tenantRepository) {

        this.repository = repository;
        this.tenantRepository = tenantRepository;
    }

    public ClienteResponse salvar(ClienteRequest clienteRequest) {
        Tenant tenant = tenantAtual();
        Cliente cliente = new Cliente();

        cliente.setNome(clienteRequest.nome());
        cliente.setEmail(clienteRequest.email());
        cliente.setEndereco(clienteRequest.endereco());
        cliente.setTelefone(normalizarTelefone(clienteRequest.telefone()));
        cliente.setTenant(tenant);

        Cliente salvo = repository.save(cliente);

        return toResponse(salvo);
    }

    public ClienteResponse atualizar(Long id, ClienteRequest clienteRequest) {
        Cliente cliente = repository.findByIdAndTenant_Id(id, tenantAtual().getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        cliente.setNome(clienteRequest.nome());
        cliente.setEmail(clienteRequest.email());
        cliente.setEndereco(clienteRequest.endereco());
        cliente.setTelefone(normalizarTelefone(clienteRequest.telefone()));
        return toResponse(repository.save(cliente));
    }
    /** Mantém só os dígitos (DDD + número) e garante o limite de 11 da coluna clientes.telefone. */
    private String normalizarTelefone(String telefone) {
        if (telefone == null) {
            return null;
        }
        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.isEmpty()) {
            return null;
        }
        if (digitos.length() > 11) {
            throw new IllegalArgumentException("Telefone deve ter no máximo 11 dígitos (DDD + número)");
        }
        return digitos;
    }

    public List<ClienteResponse> listar() {
        return repository.findAllByTenant_Id(tenantAtual().getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }
    public ClienteResponse buscarPorId(Long id) {

        return repository
                .findByIdAndTenant_Id(id, tenantAtual().getId())
                .map(this::toResponse)
                .orElse(null);
    }
    public void excluir(Long id) {
        repository.findByIdAndTenant_Id(id, tenantAtual().getId())
            .ifPresent(repository::delete);
    }

    public ClienteResponse toResponse(Cliente cliente) {

        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
            cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getEndereco()
        );

    }

    private Tenant tenantAtual() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant atual não configurado");
        }
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant não encontrado"));
    }
}