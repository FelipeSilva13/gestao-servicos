package com.felipe.gestao_servicos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.felipe.gestao_servicos.config.multitenancy.TenantContext;
import com.felipe.gestao_servicos.domain.Cliente;
import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.domain.Servico;
import com.felipe.gestao_servicos.domain.Tecnico;
import com.felipe.gestao_servicos.domain.Tenant;
import com.felipe.gestao_servicos.dto.request.OrdemRequest;
import com.felipe.gestao_servicos.dto.response.OrdemResponse;
import com.felipe.gestao_servicos.enums.StatusOS;
import com.felipe.gestao_servicos.repository.ClienteRepository;
import com.felipe.gestao_servicos.repository.OrdemServicoRepository;
import com.felipe.gestao_servicos.repository.ServicoRepository;
import com.felipe.gestao_servicos.repository.TecnicoRepository;
import com.felipe.gestao_servicos.repository.TenantRepository;

@Service
public class OrdemServicoService {
    private final OrdemServicoRepository repository;
    private final ServicoRepository servicoRepository;
    private final TecnicoRepository tecnicoRepository;
    private final ClienteRepository clienteRepository;
    private final TenantRepository tenantRepository;

    public OrdemServicoService(
            OrdemServicoRepository repository,ServicoRepository servicoRepository, TecnicoRepository tecnicoRepository, ClienteRepository clienteRepository,
           TenantRepository tenantRepository ) {
        this.repository = repository;
        this.servicoRepository = servicoRepository;
        this.clienteRepository = clienteRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.tenantRepository = tenantRepository;
    }

    private OrdemResponse toResponse(OrdemServico ordemServico) {

        return new OrdemResponse(
                ordemServico.getCliente().getId(),
                ordemServico.getTecnico().getId(),
                ordemServico.getServico().getId(),
                ordemServico.getStatus(),
                ordemServico.getDataCriacao(),
                ordemServico.getDataInicio(),
                ordemServico.getDataFim(),
                ordemServico.getObservacoes(),
                ordemServico.getValorTotal()
        );
    }

    public OrdemResponse salvar(OrdemRequest request) {
        Long tenantId = tenantIdAtual();
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalStateException("Tenant não encontrado"));

        Cliente cliente = clienteRepository.findByIdAndTenant_Id(request.clienteId(), tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado no tenant atual"));

        Tecnico tecnico = tecnicoRepository.findByIdAndTenant_Id(request.tecnicoId(), tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Técnico não encontrado no tenant atual"));

        Servico servico = servicoRepository.findByIdAndTenant_Id(request.servicoId(), tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado no tenant atual"));

        OrdemServico ordemServico = new OrdemServico();

        ordemServico.setCliente(cliente);
        ordemServico.setTecnico(tecnico);
        ordemServico.setServico(servico);
        ordemServico.setTenant(tenant);

        ordemServico.setStatus(request.status());
        ordemServico.setDataCriacao(request.dataCriacao());
        ordemServico.setDataInicio(request.dataInicio());
        ordemServico.setDataFim(request.dataFim());
        ordemServico.setObservacoes(request.observacoes());
        ordemServico.setValorTotal(request.valorTotal());

        OrdemServico salvo = repository.save(ordemServico);

        return toResponse(salvo);
    }

        public OrdemResponse atualizar(Long id, OrdemRequest request) {
        Long tenantId = tenantIdAtual();
        OrdemServico ordemServico = repository.findByIdAndTenant_Id(id, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Ordem de serviço não encontrada no tenant atual"));

        Cliente cliente = clienteRepository.findByIdAndTenant_Id(request.clienteId(), tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado no tenant atual"));
        Tecnico tecnico = tecnicoRepository.findByIdAndTenant_Id(request.tecnicoId(), tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Técnico não encontrado no tenant atual"));
        Servico servico = servicoRepository.findByIdAndTenant_Id(request.servicoId(), tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado no tenant atual"));

        ordemServico.setCliente(cliente);
        ordemServico.setTecnico(tecnico);
        ordemServico.setServico(servico);
        ordemServico.setStatus(request.status());
        ordemServico.setDataCriacao(request.dataCriacao());
        ordemServico.setDataInicio(request.dataInicio());
        ordemServico.setDataFim(request.dataFim());
        ordemServico.setObservacoes(request.observacoes());
        ordemServico.setValorTotal(request.valorTotal());

        return toResponse(repository.save(ordemServico));
        }


    public List<OrdemResponse> listar() {
        return repository.findAllByTenant_Id(tenantIdAtual())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<OrdemServico> listarEntidades() {
        return repository.findAllByTenant_Id(tenantIdAtual());
    }

    public OrdemServico buscarPorId(Long id) {
        return repository.findByIdAndTenant_Id(id, tenantIdAtual())
                .orElse(null); }

    public void excluir(Long id) {
        if (buscarPorId(id) == null) {
            throw new RuntimeException("Ordem de serviço não encontrada");
        }
        repository.deleteById(id); }

    public OrdemServico atualizarStatus(Long id, StatusOS status) {
        OrdemServico os = buscarPorId(id);
        if (os == null) return null;
        if (status == StatusOS.EM_EXECUCAO && os.getDataInicio() == null) {
            os.setDataInicio(LocalDateTime.now());
        }
        if (status == StatusOS.FINALIZADA && os.getDataFim() == null) {
            os.setDataFim(LocalDateTime.now());
            if (os.getServico() != null && os.getServico().getCusto() != null) {
                os.setValorTotal(os.getServico().getCusto());
            } else {
                os.setValorTotal(BigDecimal.ZERO);
            }
        }
        os.setStatus(status);
        return repository.save(os);
    }


    public List<OrdemResponse> listarPorCliente(Long clienteId) {

        return repository
                .findByClienteIdAndTenant_Id(clienteId, tenantIdAtual())
                .stream()
                .map(this::toResponse)
                .toList();

    }

    public List<OrdemResponse> listarPorTecnico(Long tecnicoId) {

        return repository
                .findByTecnicoIdAndTenant_Id(tecnicoId, tenantIdAtual())
                .stream()
                .map(this::toResponse)
                .toList();

    }
    public List<OrdemResponse> listarPorStatus(StatusOS status) {

        return repository
                .findByStatusAndTenant_Id(status, tenantIdAtual()).stream()
                .map(this::toResponse)
                .toList();

    }

    private Long tenantIdAtual() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant atual não configurado");
        }
        return tenantId;
    }
}