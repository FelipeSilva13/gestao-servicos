package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantContext;
import com.felipe.gestao_servicos.domain.Tecnico;
import com.felipe.gestao_servicos.dto.request.TecnicoRequest;
import com.felipe.gestao_servicos.dto.response.TecnicoResponse;
import com.felipe.gestao_servicos.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TecnicoService {
    private final TecnicoRepository repository;

    public TecnicoService(TecnicoRepository repository) {
        this.repository = repository;
    }

    private TecnicoResponse toResponse(Tecnico tecnico) {
        return new TecnicoResponse(
                tecnico.getId(),
                tecnico.getNome(),
                tecnico.getEmail(),
                tecnico.getTelefone(),
                tecnico.getEspecialidade(),
                tecnico.getDisponivel()
        );
    }

    public TecnicoResponse salvar(TecnicoRequest tecnicoRequest) {
        Tecnico tecnico = new Tecnico();
        tecnico.setNome(tecnicoRequest.nome());
        tecnico.setEmail(tecnicoRequest.email());
        tecnico.setTelefone(tecnicoRequest.telefone());
        tecnico.setEspecialidade(tecnicoRequest.especialidade());
        tecnico.setDisponivel(tecnicoRequest.disponivel() != null
                ? tecnicoRequest.disponivel()
                : true);

        return toResponse(repository.save(tecnico));
    }

    public List<TecnicoResponse> listar() {
        return repository.findAll()
                .stream()
                .filter(this::mesmoTenant)
                .map(this::toResponse)
                .toList();

    }

    public TecnicoResponse buscarPorId(Long id) {
        return repository.findById(id)
                .filter(this::mesmoTenant)
                .map(this::toResponse)
                .orElse(null);
    }

    public TecnicoResponse atualizar(Long id, TecnicoRequest tecnicoRequest) {
        Tecnico tecnico = repository.findById(id)
                .filter(this::mesmoTenant)
                .orElse(null);

        if (tecnico == null) {
            return null;
        }

        tecnico.setNome(tecnicoRequest.nome());
        tecnico.setEmail(tecnicoRequest.email());
        tecnico.setTelefone(tecnicoRequest.telefone());
        tecnico.setEspecialidade(tecnicoRequest.especialidade());
        if (tecnicoRequest.disponivel() != null) {
            tecnico.setDisponivel(tecnicoRequest.disponivel());
        }

        return toResponse(repository.save(tecnico));
    }

    public void excluir(Long id) {
        repository.findById(id)
                .filter(this::mesmoTenant)
                .ifPresent(repository::delete);
    }

    private boolean mesmoTenant(Tecnico tecnico) {
        Long tenantId = TenantContext.getCurrentTenant();
        return tenantId != null
                && tecnico.getTenant() != null
                && tenantId.equals(tecnico.getTenant().getId());
    }
}