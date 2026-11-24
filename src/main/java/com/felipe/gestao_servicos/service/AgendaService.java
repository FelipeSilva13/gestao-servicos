package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantContext;
import com.felipe.gestao_servicos.domain.DiaAtendimento;
import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.enums.StatusOS;
import com.felipe.gestao_servicos.repository.DiaAtendimentoRepository;
import com.felipe.gestao_servicos.repository.OrdemServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Agenda de atendimentos: dias abertos pela empresa e horários livres.
 *
 * <p>Regra de conflito: dois atendimentos da mesma empresa precisam ficar a pelo menos
 * {@link #INTERVALO_MINIMO} de distância. Ordens encerradas não ocupam a agenda.</p>
 */
@Service
public class AgendaService {

    /** Distância mínima entre dois atendimentos. */
    public static final Duration INTERVALO_MINIMO = Duration.ofHours(4);
    /** Os horários oferecidos começam na hora cheia, de hora em hora. */
    public static final Duration PASSO = Duration.ofHours(1);
    public static final LocalTime INICIO_PADRAO = LocalTime.of(8, 0);
    public static final LocalTime FIM_PADRAO = LocalTime.of(18, 0);

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public enum Situacao {
        /** A empresa não abriu este dia. */
        FECHADO,
        /** Aberto e com pelo menos um horário livre. */
        DISPONIVEL,
        /** Aberto, mas todos os horários já estão ocupados ou bloqueados pelo intervalo. */
        LOTADO,
        /** Dia que já passou (ou cujos horários de hoje já passaram). */
        PASSADO
    }

    /** Atendimento que ocupa a agenda (só exposto à equipe). */
    public record Ocupacao(Long ordemId, String numero, LocalDateTime dataHora, boolean confirmado, String cliente) {}

    public record Dia(LocalDate data, Situacao situacao, LocalTime inicio, LocalTime fim,
                      List<LocalTime> horariosLivres, List<Ocupacao> ocupacoes) {
        public boolean isAberto() {
            return inicio != null;
        }
    }

    private final DiaAtendimentoRepository diaRepository;
    private final OrdemServicoRepository ordemRepository;
    private final Clock clock;

    @Autowired
    public AgendaService(DiaAtendimentoRepository diaRepository, OrdemServicoRepository ordemRepository) {
        this(diaRepository, ordemRepository, Clock.systemDefaultZone());
    }

    AgendaService(DiaAtendimentoRepository diaRepository, OrdemServicoRepository ordemRepository, Clock clock) {
        this.diaRepository = diaRepository;
        this.ordemRepository = ordemRepository;
        this.clock = clock;
    }

    // ---- Consulta -------------------------------------------------------------

    /**
     * Situação de cada dia do mês.
     *
     * @param ignorarOrdemId ordem que está sendo remarcada: o horário atual dela não conta como ocupado
     */
    @Transactional(readOnly = true)
    public List<Dia> mes(YearMonth mes, Long ignorarOrdemId) {
        Long tenantId = tenantAtual();
        LocalDate primeiro = mes.atDay(1);
        LocalDate ultimo = mes.atEndOfMonth();

        Map<LocalDate, DiaAtendimento> abertos = diaRepository
                .findByTenantIdAndDataBetweenOrderByData(tenantId, primeiro, ultimo).stream()
                .collect(Collectors.toMap(DiaAtendimento::getData, Function.identity()));
        // Margem de um intervalo antes e depois: um atendimento às 23h do dia anterior bloqueia o início do dia 1.
        List<OrdemServico> ocupadas = ocupadas(tenantId,
                primeiro.atStartOfDay().minus(INTERVALO_MINIMO),
                ultimo.plusDays(1).atStartOfDay().plus(INTERVALO_MINIMO), ignorarOrdemId);

        LocalDateTime agora = LocalDateTime.now(clock);
        List<Dia> dias = new ArrayList<>(ultimo.getDayOfMonth());
        for (LocalDate d = primeiro; !d.isAfter(ultimo); d = d.plusDays(1)) {
            dias.add(montarDia(d, abertos.get(d), ocupadas, agora));
        }
        return dias;
    }

    /** Horários livres de um único dia. */
    @Transactional(readOnly = true)
    public Dia dia(LocalDate data, Long ignorarOrdemId) {
        Long tenantId = tenantAtual();
        DiaAtendimento aberto = diaRepository.findByTenantIdAndData(tenantId, data).orElse(null);
        List<OrdemServico> ocupadas = ocupadas(tenantId,
                data.atStartOfDay().minus(INTERVALO_MINIMO),
                data.plusDays(1).atStartOfDay().plus(INTERVALO_MINIMO), ignorarOrdemId);
        return montarDia(data, aberto, ocupadas, LocalDateTime.now(clock));
    }

    // ---- Validação usada por quem marca horário ---------------------------------

    /**
     * Garante que o horário pode ser marcado: dia aberto, dentro da janela, no futuro e
     * a pelo menos 4 horas de qualquer outro atendimento. Trava os dias envolvidos até o
     * fim da transação de quem chamou, evitando que dois pedidos simultâneos peguem o mesmo horário.
     */
    @Transactional
    public void validarHorario(LocalDateTime dataHora, Long ordemId) {
        if (dataHora == null) {
            throw new RegraNegocioException("Escolha a data e o horário do atendimento");
        }
        if (!dataHora.isAfter(LocalDateTime.now(clock))) {
            throw new RegraNegocioException("Escolha um horário futuro para o atendimento");
        }
        Long tenantId = tenantAtual();
        LocalDate data = dataHora.toLocalDate();

        // A ordem por data na consulta evita deadlock entre dois pedidos em dias vizinhos.
        DiaAtendimento dia = diaRepository
                .findForUpdateByTenantIdAndDataBetweenOrderByData(tenantId, data.minusDays(1), data.plusDays(1)).stream()
                .filter(d -> d.getData().equals(data))
                .findFirst()
                .orElseThrow(() -> new RegraNegocioException(
                        "A empresa não abriu a agenda em " + data.format(DATA) + ". Escolha um dia destacado no calendário."));

        LocalTime hora = dataHora.toLocalTime();
        if (hora.isBefore(dia.getHoraInicio()) || hora.isAfter(ultimoHorario(dia))) {
            throw new RegraNegocioException("Em " + data.format(DATA) + " os atendimentos começam entre "
                    + dia.getHoraInicio().format(HORA) + " e " + ultimoHorario(dia).format(HORA) + ".");
        }

        ocupadas(tenantId, dataHora.minus(INTERVALO_MINIMO), dataHora.plus(INTERVALO_MINIMO), ordemId).stream()
                .filter(o -> conflita(o.getDataAgendada(), dataHora))
                .findFirst()
                .ifPresent(o -> {
                    throw new RegraNegocioException("Horário indisponível: já existe um atendimento às "
                            + o.getDataAgendada().format(HORA) + " e é preciso um intervalo de "
                            + INTERVALO_MINIMO.toHours() + " horas entre atendimentos.");
                });
    }

    // ---- Gestão dos dias (equipe) -------------------------------------------

    /** Abre (ou ajusta a janela de) vários dias de uma vez. */
    @Transactional
    public List<DiaAtendimento> abrirDias(Collection<LocalDate> datas, LocalTime inicio, LocalTime fim) {
        if (datas == null || datas.isEmpty()) {
            throw new RegraNegocioException("Selecione ao menos um dia");
        }
        if (datas.size() > 62) {
            throw new RegraNegocioException("Abra no máximo 62 dias por vez");
        }
        LocalTime ini = inicio == null ? INICIO_PADRAO : inicio;
        LocalTime fi = fim == null ? FIM_PADRAO : fim;
        if (Duration.between(ini, fi).compareTo(PASSO) < 0) {
            throw new RegraNegocioException("O horário de fechamento deve ser pelo menos 1 hora depois da abertura");
        }

        Long tenantId = tenantAtual();
        LocalDate hoje = LocalDate.now(clock);
        List<DiaAtendimento> salvos = new ArrayList<>();
        for (LocalDate data : datas.stream().filter(Objects::nonNull).distinct().sorted().toList()) {
            if (data.isBefore(hoje)) {
                throw new RegraNegocioException("Não é possível abrir " + data.format(DATA) + ": a data já passou");
            }
            DiaAtendimento dia = diaRepository.findByTenantIdAndData(tenantId, data).orElseGet(() -> {
                DiaAtendimento novo = new DiaAtendimento();
                novo.setTenantId(tenantId);
                novo.setData(data);
                return novo;
            });
            dia.setHoraInicio(ini);
            dia.setHoraFim(fi);
            garantirAtendimentosDentroDaJanela(dia, tenantId);
            salvos.add(diaRepository.save(dia));
        }
        return salvos;
    }

    /** Fecha o dia; recusa se ainda houver atendimento marcado nele. */
    @Transactional
    public void fecharDia(LocalDate data) {
        Long tenantId = tenantAtual();
        DiaAtendimento dia = diaRepository.findByTenantIdAndData(tenantId, data).orElse(null);
        if (dia == null) {
            return;
        }
        long marcados = ocupadas(tenantId, data.atStartOfDay(), data.plusDays(1).atStartOfDay().minusNanos(1), null).size();
        if (marcados > 0) {
            throw new RegraNegocioException("Há " + marcados + " atendimento(s) marcado(s) em " + data.format(DATA)
                    + ". Remarque-os antes de fechar o dia.");
        }
        diaRepository.delete(dia);
    }

    // ---- Internos ---------------------------------------------------------------

    private Dia montarDia(LocalDate data, DiaAtendimento aberto, List<OrdemServico> ocupadas, LocalDateTime agora) {
        List<Ocupacao> doDia = ocupadas.stream()
                .filter(o -> o.getDataAgendada().toLocalDate().equals(data))
                .map(o -> new Ocupacao(o.getId(), o.getNumero(), o.getDataAgendada(), o.isAgendamentoConfirmado(),
                        o.getCliente() == null ? null : o.getCliente().getNome()))
                .toList();

        if (aberto == null) {
            Situacao s = data.isBefore(agora.toLocalDate()) ? Situacao.PASSADO : Situacao.FECHADO;
            return new Dia(data, s, null, null, List.of(), doDia);
        }

        List<LocalTime> livres = new ArrayList<>();
        boolean algumFuturo = false;
        for (LocalTime h = aberto.getHoraInicio(); !h.isAfter(ultimoHorario(aberto)); h = h.plus(PASSO)) {
            LocalDateTime inicio = data.atTime(h);
            if (!inicio.isAfter(agora)) {
                continue;
            }
            algumFuturo = true;
            if (ocupadas.stream().noneMatch(o -> conflita(o.getDataAgendada(), inicio))) {
                livres.add(h);
            }
        }

        Situacao situacao = !algumFuturo ? Situacao.PASSADO
                : livres.isEmpty() ? Situacao.LOTADO
                : Situacao.DISPONIVEL;
        return new Dia(data, situacao, aberto.getHoraInicio(), aberto.getHoraFim(), List.copyOf(livres), doDia);
    }

    private void garantirAtendimentosDentroDaJanela(DiaAtendimento dia, Long tenantId) {
        LocalDate data = dia.getData();
        LocalTime ultimo = ultimoHorario(dia);
        ocupadas(tenantId, data.atStartOfDay(), data.plusDays(1).atStartOfDay().minusNanos(1), null).stream()
                .filter(o -> {
                    LocalTime h = o.getDataAgendada().toLocalTime();
                    return h.isBefore(dia.getHoraInicio()) || h.isAfter(ultimo);
                })
                .findFirst()
                .ifPresent(o -> {
                    throw new RegraNegocioException("A " + o.getNumero() + " está marcada às "
                            + o.getDataAgendada().format(HORA) + " em " + data.format(DATA)
                            + ", fora da nova janela. Remarque-a antes de alterar o horário.");
                });
    }

    /** Último horário de início possível: uma hora antes do fechamento. */
    private static LocalTime ultimoHorario(DiaAtendimento dia) {
        return dia.getHoraFim().minus(PASSO);
    }

    private static boolean conflita(LocalDateTime existente, LocalDateTime pretendido) {
        return Duration.between(existente, pretendido).abs().compareTo(INTERVALO_MINIMO) < 0;
    }

    private List<OrdemServico> ocupadas(Long tenantId, LocalDateTime de, LocalDateTime ate, Long ignorarOrdemId) {
        return ordemRepository
                .findByTenant_IdAndDataAgendadaBetweenAndStatusNotInOrderByDataAgendada(tenantId, de, ate, StatusOS.ENCERRADOS)
                .stream()
                .filter(o -> ignorarOrdemId == null || !ignorarOrdemId.equals(o.getId()))
                .toList();
    }

    private static Long tenantAtual() {
        Long tenantId = TenantContext.getCurrentTenant();
        return tenantId != null ? tenantId : UsuarioAtual.obrigatorio().getTenantId();
    }
}
