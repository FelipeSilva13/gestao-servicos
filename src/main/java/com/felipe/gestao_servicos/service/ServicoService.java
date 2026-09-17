package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.domain.Servico;
import com.felipe.gestao_servicos.domain.Tenant;
import com.felipe.gestao_servicos.config.multitenancy.TenantContext;
import com.felipe.gestao_servicos.dto.request.ServicoRequest;
import com.felipe.gestao_servicos.dto.response.ServicoResponse;
import com.felipe.gestao_servicos.repository.ServicoRepository;
import com.felipe.gestao_servicos.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicoService {
    private final ServicoRepository repository;
    private final TenantRepository tenantRepository;

    public ServicoService(ServicoRepository repository, TenantRepository tenantRepository) {
        this.repository = repository;
        this.tenantRepository = tenantRepository;
    }

    public ServicoResponse salvar(ServicoRequest dto) {

        if (dto.tempoEstimadoMinutos() > 1440) {
            throw new IllegalArgumentException(
                    "O tempo estimado não pode ser maior que 1440 minutos."
            );
        }

        Tenant tenant = tenantAtual();
        Servico servico = new Servico();

        servico.setDescricao(dto.descricao());
        servico.setTipo(dto.tipo());
        servico.setCusto(dto.custo());
        servico.setTempoEstimadoMinutos(dto.tempoEstimadoMinutos());
        servico.setTenant(tenant);

        Servico salvo = repository.save(servico);

        return toResponse(salvo);
    }

    public List<ServicoResponse> listar() {
        return repository.findAllByTenant_Id(tenantAtual().getId())
            .stream()
                .map(this::toResponse)
                .toList();

    }
    public ServicoResponse buscarPorId(Long id) {

        return repository.findByIdAndTenant_Id(id, tenantAtual().getId())
                .map(this::toResponse)
                .orElseThrow(() ->
                        new RuntimeException("Serviço não encontrado"));

    }

    public ServicoResponse atualizar(Long id, ServicoRequest dto) {

        if (dto.tempoEstimadoMinutos() > 1440) {
            throw new IllegalArgumentException(
                    "O tempo estimado não pode ser maior que 1440 minutos."
            );
        }

        Servico servico = repository.findByIdAndTenant_Id(id, tenantAtual().getId())
                .orElseThrow(() ->
                        new RuntimeException("Serviço não encontrado"));

        servico.setDescricao(dto.descricao());
        servico.setTipo(dto.tipo());
        servico.setCusto(dto.custo());
        servico.setTempoEstimadoMinutos(dto.tempoEstimadoMinutos());

        Servico atualizado = repository.save(servico);

        return toResponse(atualizado);
    }

    public void excluir(Long id) {
        repository.findByIdAndTenant_Id(id, tenantAtual().getId())
            .ifPresent(repository::delete);
    }
    private ServicoResponse toResponse(Servico servico) {

        return new ServicoResponse(
                servico.getId(),
                servico.getDescricao(),
                servico.getTipo(),
                servico.getCusto(),
                servico.getTempoEstimadoMinutos()
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