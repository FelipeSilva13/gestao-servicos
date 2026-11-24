package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantUserDetails;
import com.felipe.gestao_servicos.domain.Avaliacao;
import com.felipe.gestao_servicos.domain.Cliente;
import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.domain.Servico;
import com.felipe.gestao_servicos.enums.StatusOS;
import com.felipe.gestao_servicos.repository.AvaliacaoRepository;
import com.felipe.gestao_servicos.repository.ClienteRepository;
import com.felipe.gestao_servicos.repository.OrdemServicoRepository;
import com.felipe.gestao_servicos.repository.ServicoRepository;
import com.felipe.gestao_servicos.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Casos de uso do portal: sempre restritos às ordens do cliente autenticado. */
@Service
public class PortalClienteService {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final Set<StatusOS> CANCELAVEIS = EnumSet.of(StatusOS.SOLICITADA, StatusOS.AGUARDANDO_APROVACAO, StatusOS.PENDENTE);

    private final OrdemServicoRepository ordemRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final TenantRepository tenantRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final AnexoService anexoService;
    private final EventoOrdemService eventos;
    private final AgendaService agenda;

    public PortalClienteService(OrdemServicoRepository ordemRepository, ClienteRepository clienteRepository,
                                ServicoRepository servicoRepository, TenantRepository tenantRepository,
                                AvaliacaoRepository avaliacaoRepository, AnexoService anexoService,
                                EventoOrdemService eventos, AgendaService agenda) {
        this.ordemRepository = ordemRepository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
        this.tenantRepository = tenantRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.anexoService = anexoService;
        this.eventos = eventos;
        this.agenda = agenda;
    }

    // ---- Consulta / histórico -------------------------------------------------

    public Cliente clienteAtual() {
        TenantUserDetails u = usuario();
        return clienteRepository.findByIdAndTenant_Id(u.getClienteId(), u.getTenantId())
                .orElseThrow(() -> new IllegalStateException("Cliente do portal não encontrado"));
    }

    public List<OrdemServico> minhasOrdens() {
        TenantUserDetails u = usuario();
        return ordemRepository.findByCliente_IdAndTenant_IdOrderByDataCriacaoDesc(u.getClienteId(), u.getTenantId());
    }

    public OrdemServico minhaOrdem(Long id) {
        TenantUserDetails u = usuario();
        return ordemRepository.findByIdAndCliente_IdAndTenant_Id(id, u.getClienteId(), u.getTenantId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada"));
    }

    public Optional<Avaliacao> avaliacao(OrdemServico os) {
        return avaliacaoRepository.findByOrdemIdAndTenantId(os.getId(), os.getTenant().getId());
    }

    /** Ids das ordens já avaliadas, para destacar avaliações pendentes na listagem. */
    public Set<Long> idsAvaliados(List<OrdemServico> ordens) {
        if (ordens.isEmpty()) return Set.of();
        return avaliacaoRepository.findByTenantIdAndOrdemIdIn(usuario().getTenantId(),
                        ordens.stream().map(OrdemServico::getId).toList())
                .stream().map(Avaliacao::getOrdemId).collect(Collectors.toSet());
    }

    public List<Servico> catalogoServicos() {
        return servicoRepository.findAllByTenant_Id(usuario().getTenantId());
    }

    // ---- Solicitação de atendimento ------------------------------------------

    @Transactional
    public OrdemServico solicitar(String descricao, Long servicoId, LocalDateTime dataPreferida, List<MultipartFile> arquivos) {
        if (descricao == null || descricao.isBlank()) {
            throw new RegraNegocioException("Descreva o problema ou o serviço que você precisa");
        }
        if (descricao.length() > 2000) {
            throw new RegraNegocioException("A descrição deve ter no máximo 2000 caracteres");
        }
        if (dataPreferida != null) {
            agenda.validarHorario(dataPreferida, null);
        }

        TenantUserDetails u = usuario();
        OrdemServico os = new OrdemServico();
        os.setCliente(clienteAtual());
        os.setTenant(tenantRepository.findById(u.getTenantId())
                .orElseThrow(() -> new IllegalStateException("Tenant não encontrado")));
        if (servicoId != null) {
            os.setServico(servicoRepository.findByIdAndTenant_Id(servicoId, u.getTenantId())
                    .orElseThrow(() -> new RegraNegocioException("Serviço não encontrado")));
        }
        os.setStatus(StatusOS.SOLICITADA);
        os.setDataCriacao(LocalDateTime.now());
        os.setDescricaoProblema(descricao.trim());
        os.setDataAgendada(dataPreferida);
        os.setAgendamentoConfirmado(false);
        OrdemServico salvo = ordemRepository.save(os);

        anexoService.anexar(salvo, arquivos);
        String detalhe = dataPreferida == null ? "" : " (data preferida: " + dataPreferida.format(DATA_HORA) + ")";
        eventos.registrar(salvo, "Nova solicitação aberta por " + salvo.getCliente().getNome() + detalhe,
                EventoOrdemService.Destino.EQUIPE);
        return salvo;
    }

    // ---- Orçamento ----------------------------------------------------------

    @Transactional
    public OrdemServico aprovarOrcamento(Long id) {
        OrdemServico os = minhaOrdem(id);
        exigirOrcamentoPendente(os);
        os.setOrcamentoRespondidoEm(LocalDateTime.now());
        os.setValorTotal(os.getOrcamentoValor());
        os.setStatus(StatusOS.PENDENTE);
        OrdemServico salvo = ordemRepository.save(os);
        eventos.registrar(salvo, "Orçamento aprovado pelo cliente", EventoOrdemService.Destino.EQUIPE);
        return salvo;
    }

    @Transactional
    public OrdemServico recusarOrcamento(Long id, String motivo) {
        OrdemServico os = minhaOrdem(id);
        exigirOrcamentoPendente(os);
        String motivoLimpo = motivo == null || motivo.isBlank() ? null : motivo.trim();
        if (motivoLimpo != null && motivoLimpo.length() > 500) {
            motivoLimpo = motivoLimpo.substring(0, 500);
        }
        os.setOrcamentoRespondidoEm(LocalDateTime.now());
        os.setOrcamentoMotivoRecusa(motivoLimpo);
        os.setStatus(StatusOS.RECUSADA);
        OrdemServico salvo = ordemRepository.save(os);
        eventos.registrar(salvo, "Orçamento recusado pelo cliente" + (motivoLimpo == null ? "" : ": " + motivoLimpo),
                EventoOrdemService.Destino.EQUIPE);
        return salvo;
    }

    @Transactional
    public OrdemServico cancelar(Long id) {
        OrdemServico os = minhaOrdem(id);
        if (!CANCELAVEIS.contains(os.getStatus())) {
            throw new RegraNegocioException("Esta ordem não pode mais ser cancelada pelo portal");
        }
        os.setStatus(StatusOS.CANCELADA);
        OrdemServico salvo = ordemRepository.save(os);
        eventos.registrar(salvo, "Solicitação cancelada pelo cliente", EventoOrdemService.Destino.EQUIPE);
        return salvo;
    }

    // ---- Agendamento ---------------------------------------------------------

    @Transactional
    public OrdemServico reagendar(Long id, LocalDateTime dataHora) {
        OrdemServico os = minhaOrdem(id);
        if (os.getStatus().isEncerrado() || os.getStatus() == StatusOS.EM_EXECUCAO) {
            throw new RegraNegocioException("Não é possível alterar a data desta ordem");
        }
        if (dataHora == null) {
            throw new RegraNegocioException("Escolha a data e o horário desejados");
        }
        agenda.validarHorario(dataHora, os.getId());
        os.setDataAgendada(dataHora);
        os.setAgendamentoConfirmado(false);
        OrdemServico salvo = ordemRepository.save(os);
        eventos.registrar(salvo, "Cliente solicitou atendimento para " + dataHora.format(DATA_HORA),
                EventoOrdemService.Destino.EQUIPE);
        return salvo;
    }

    // ---- Arquivos -------------------------------------------------------------

    @Transactional
    public int anexar(Long id, List<MultipartFile> arquivos) {
        OrdemServico os = minhaOrdem(id);
        if (os.getStatus() == StatusOS.CANCELADA) {
            throw new RegraNegocioException("Não é possível anexar arquivos a uma ordem cancelada");
        }
        int quantidade = anexoService.anexar(os, arquivos);
        if (quantidade == 0) {
            throw new RegraNegocioException("Selecione ao menos um arquivo");
        }
        eventos.registrar(os, "Cliente anexou " + quantidade + (quantidade == 1 ? " arquivo" : " arquivos"),
                EventoOrdemService.Destino.EQUIPE);
        return quantidade;
    }

    // ---- Avaliação ------------------------------------------------------------

    @Transactional
    public Avaliacao avaliar(Long id, Integer notaServico, Integer notaTecnico, String comentario) {
        OrdemServico os = minhaOrdem(id);
        if (os.getStatus() != StatusOS.FINALIZADA) {
            throw new RegraNegocioException("A avaliação fica disponível após a conclusão do serviço");
        }
        if (avaliacaoRepository.existsByOrdemIdAndTenantId(os.getId(), os.getTenant().getId())) {
            throw new RegraNegocioException("Este atendimento já foi avaliado");
        }
        validarNota(notaServico, "serviço", true);
        validarNota(notaTecnico, "técnico", false);

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setOrdemId(os.getId());
        avaliacao.setTenantId(os.getTenant().getId());
        avaliacao.setNotaServico(notaServico);
        avaliacao.setNotaTecnico(os.getTecnico() == null ? null : notaTecnico);
        String texto = comentario == null || comentario.isBlank() ? null : comentario.trim();
        avaliacao.setComentario(texto != null && texto.length() > 1000 ? texto.substring(0, 1000) : texto);
        Avaliacao salva = avaliacaoRepository.save(avaliacao);

        eventos.registrar(os, "Cliente avaliou o atendimento com nota " + notaServico + "/5",
                EventoOrdemService.Destino.EQUIPE);
        return salva;
    }

    // ---- Apoio -----------------------------------------------------------------

    /** Contagem por status para os cards do painel. */
    public Map<StatusOS, Long> contagemPorStatus(List<OrdemServico> ordens) {
        return ordens.stream().collect(Collectors.groupingBy(OrdemServico::getStatus, Collectors.counting()));
    }

    private void exigirOrcamentoPendente(OrdemServico os) {
        if (os.getStatus() != StatusOS.AGUARDANDO_APROVACAO || os.getOrcamentoValor() == null) {
            throw new RegraNegocioException("Não há orçamento aguardando sua aprovação nesta ordem");
        }
    }

    private static void validarNota(Integer nota, String alvo, boolean obrigatoria) {
        if (nota == null) {
            if (obrigatoria) throw new RegraNegocioException("Dê uma nota de 1 a 5 para o " + alvo);
            return;
        }
        if (nota < 1 || nota > 5) {
            throw new RegraNegocioException("A nota do " + alvo + " deve ser de 1 a 5");
        }
    }

    private static TenantUserDetails usuario() {
        TenantUserDetails u = UsuarioAtual.obrigatorio();
        if (!u.isCliente()) {
            throw new IllegalStateException("Acesso restrito ao portal do cliente");
        }
        return u;
    }
}
