package com.felipe.gestao_servicos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final EventoOrdemService eventos;

    public OrdemServicoService(
            OrdemServicoRepository repository,ServicoRepository servicoRepository, TecnicoRepository tecnicoRepository, ClienteRepository clienteRepository,
           TenantRepository tenantRepository, EventoOrdemService eventos) {
        this.eventos = eventos;
        this.repository = repository;
        this.servicoRepository = servicoRepository;
        this.clienteRepository = clienteRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.tenantRepository = tenantRepository;
    }

    private OrdemResponse toResponse(OrdemServico ordemServico) {

        return new OrdemResponse(
                ordemServico.getCliente().getId(),
                ordemServico.getTecnico() != null ? ordemServico.getTecnico().getId() : null,
                ordemServico.getServico() != null ? ordemServico.getServico().getId() : null,
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
        eventos.registrar(salvo, "Ordem de serviço aberta pela empresa", EventoOrdemService.Destino.CLIENTE);

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

    @Transactional
    public OrdemServico atualizarStatus(Long id, StatusOS status) {
        OrdemServico os = buscarPorId(id);
        if (os == null) return null;
        if (status == os.getStatus()) return os;
        if (status == StatusOS.EM_EXECUCAO && os.getDataInicio() == null) {
            os.setDataInicio(LocalDateTime.now());
        }
        if (status == StatusOS.FINALIZADA && os.getDataFim() == null) {
            os.setDataFim(LocalDateTime.now());
            if (os.getValorTotal() == null) {
                if (os.getOrcamentoValor() != null && os.getOrcamentoRespondidoEm() != null) {
                    os.setValorTotal(os.getOrcamentoValor());
                } else if (os.getServico() != null && os.getServico().getCusto() != null) {
                    os.setValorTotal(os.getServico().getCusto());
                } else {
                    os.setValorTotal(BigDecimal.ZERO);
                }
            }
        }
        os.setStatus(status);
        OrdemServico salvo = repository.save(os);
        String descricao = status == StatusOS.FINALIZADA
                ? "Atendimento concluído. Conte como foi avaliando o serviço."
                : "Status alterado para " + status.getLabel();
        eventos.registrar(salvo, descricao, EventoOrdemService.Destino.CLIENTE);
        return salvo;
    }

    /** Atribui técnico e/ou serviço (usado na triagem das solicitações do portal). */
    @Transactional
    public OrdemServico atribuir(Long id, Long tecnicoId, Long servicoId) {
        Long tenantId = tenantIdAtual();
        OrdemServico os = buscarObrigatoria(id);
        Tecnico tecnico = tecnicoId == null ? null : tecnicoRepository.findByIdAndTenant_Id(tecnicoId, tenantId)
                .orElseThrow(() -> new RegraNegocioException("Técnico não encontrado"));
        Servico servico = servicoId == null ? null : servicoRepository.findByIdAndTenant_Id(servicoId, tenantId)
                .orElseThrow(() -> new RegraNegocioException("Serviço não encontrado"));

        boolean trocouTecnico = tecnico != null && (os.getTecnico() == null || !tecnico.getId().equals(os.getTecnico().getId()));
        os.setTecnico(tecnico);
        os.setServico(servico);
        OrdemServico salvo = repository.save(os);
        if (trocouTecnico) {
            eventos.registrar(salvo, "Técnico responsável: " + tecnico.getNome(), EventoOrdemService.Destino.CLIENTE);
        }
        return salvo;
    }

    /** Envia (ou reenvia) o orçamento para aprovação do cliente. */
    @Transactional
    public OrdemServico enviarOrcamento(Long id, BigDecimal valor, String descricao) {
        OrdemServico os = buscarObrigatoria(id);
        if (os.getStatus() == StatusOS.EM_EXECUCAO || os.getStatus().isEncerrado() && os.getStatus() != StatusOS.RECUSADA) {
            throw new RegraNegocioException("Não é possível enviar orçamento para uma ordem " + os.getStatus().getLabel().toLowerCase());
        }
        if (valor == null || valor.signum() < 0) {
            throw new RegraNegocioException("Informe um valor de orçamento válido");
        }
        os.setOrcamentoValor(valor);
        os.setOrcamentoDescricao(descricao == null || descricao.isBlank() ? null : descricao.trim());
        os.setOrcamentoEnviadoEm(LocalDateTime.now());
        os.setOrcamentoRespondidoEm(null);
        os.setOrcamentoMotivoRecusa(null);
        os.setStatus(StatusOS.AGUARDANDO_APROVACAO);
        OrdemServico salvo = repository.save(os);
        eventos.registrar(salvo, "Orçamento de R$ " + String.format(java.util.Locale.of("pt", "BR"), "%,.2f", valor)
                + " enviado para sua aprovação", EventoOrdemService.Destino.CLIENTE);
        return salvo;
    }

    /** Define a data do atendimento; confirmada = a empresa garante o horário. */
    @Transactional
    public OrdemServico agendar(Long id, LocalDateTime dataHora, boolean confirmado) {
        OrdemServico os = buscarObrigatoria(id);
        if (dataHora == null) {
            throw new RegraNegocioException("Informe a data e hora do atendimento");
        }
        os.setDataAgendada(dataHora);
        os.setAgendamentoConfirmado(confirmado);
        OrdemServico salvo = repository.save(os);
        String quando = dataHora.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
        eventos.registrar(salvo, (confirmado ? "Atendimento confirmado para " : "Nova data proposta para o atendimento: ") + quando,
                EventoOrdemService.Destino.CLIENTE);
        return salvo;
    }

    public OrdemServico buscarObrigatoria(Long id) {
        OrdemServico os = buscarPorId(id);
        if (os == null) {
            throw new RecursoNaoEncontradoException("Ordem de serviço não encontrada");
        }
        return os;
    }

    public List<OrdemServico> listarEntidadesRecentes() {
        return repository.findAllByTenant_IdOrderByDataCriacaoDesc(tenantIdAtual());
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