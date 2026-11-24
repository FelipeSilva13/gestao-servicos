package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantContext;
import com.felipe.gestao_servicos.domain.DiaAtendimento;
import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.enums.StatusOS;
import com.felipe.gestao_servicos.repository.DiaAtendimentoRepository;
import com.felipe.gestao_servicos.repository.OrdemServicoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AgendaServiceTest {

    private static final Long TENANT = 1L;
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    /** "Agora" fixo: 1º de dezembro de 2026, 09:00. */
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 12, 1, 9, 0);
    private static final LocalDate DIA = LocalDate.of(2026, 12, 10);

    @Mock DiaAtendimentoRepository diaRepository;
    @Mock OrdemServicoRepository ordemRepository;

    AgendaService agenda;
    List<OrdemServico> marcadas;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(TENANT);
        agenda = new AgendaService(diaRepository, ordemRepository, Clock.fixed(AGORA.atZone(ZONA).toInstant(), ZONA));
        marcadas = new ArrayList<>();
        when(ordemRepository.findByTenant_IdAndDataAgendadaBetweenAndStatusNotInOrderByDataAgendada(
                eq(TENANT), any(), any(), eq(StatusOS.ENCERRADOS)))
                .thenAnswer(inv -> {
                    LocalDateTime de = inv.getArgument(1);
                    LocalDateTime ate = inv.getArgument(2);
                    return marcadas.stream()
                            .filter(o -> !o.getDataAgendada().isBefore(de) && !o.getDataAgendada().isAfter(ate))
                            .toList();
                });
        abrir(DIA, 8, 18);
    }

    @AfterEach
    void limpar() {
        TenantContext.clear();
    }

    // ---- regra das 4 horas ------------------------------------------------------

    @Test
    void recusaHorarioAMenosDeQuatroHorasDeOutroAtendimento() {
        marcar(1L, DIA.atTime(10, 0));

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> agenda.validarHorario(DIA.atTime(13, 0), null));
        assertTrue(erro.getMessage().contains("10:00"), erro.getMessage());
        assertThrows(RegraNegocioException.class, () -> agenda.validarHorario(DIA.atTime(11, 0), null));
    }

    @Test
    void aceitaHorarioExatamenteQuatroHorasDepois() {
        marcar(1L, DIA.atTime(10, 0));

        assertDoesNotThrow(() -> agenda.validarHorario(DIA.atTime(14, 0), null));
    }

    @Test
    void aoRemarcarOHorarioDaPropriaOrdemNaoContaComoConflito() {
        marcar(1L, DIA.atTime(10, 0));

        assertDoesNotThrow(() -> agenda.validarHorario(DIA.atTime(11, 0), 1L));
    }

    @Test
    void recusaDiaQueAEmpresaNaoAbriu() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> agenda.validarHorario(DIA.plusDays(1).atTime(10, 0), null));
        assertTrue(erro.getMessage().contains("não abriu"), erro.getMessage());
    }

    @Test
    void recusaHorarioForaDaJanelaDoDia() {
        assertThrows(RegraNegocioException.class, () -> agenda.validarHorario(DIA.atTime(7, 0), null));
        // Fecha às 18h: o último início possível é 17h.
        assertThrows(RegraNegocioException.class, () -> agenda.validarHorario(DIA.atTime(18, 0), null));
        assertDoesNotThrow(() -> agenda.validarHorario(DIA.atTime(17, 0), null));
    }

    @Test
    void recusaHorarioNoPassado() {
        abrir(AGORA.toLocalDate(), 8, 18);
        assertThrows(RegraNegocioException.class, () -> agenda.validarHorario(AGORA.minusHours(1), null));
    }

    // ---- calendário do mês ---------------------------------------------------------

    @Test
    void mesMostraDisponivelLotadoEFechado() {
        LocalDate lotado = LocalDate.of(2026, 12, 11);
        abrir(lotado, 8, 12); // horários 8h..11h
        marcar(2L, lotado.atTime(9, 0)); // 9h bloqueia de 5h a 13h: nada sobra
        marcar(1L, DIA.atTime(10, 0));

        List<AgendaService.Dia> dias = agenda.mes(YearMonth.of(2026, 12), null);

        assertEquals(31, dias.size());
        AgendaService.Dia dia10 = dias.get(9);
        assertEquals(AgendaService.Situacao.DISPONIVEL, dia10.situacao());
        // Com 10h marcado, 8h a 13h ficam a menos de 4 horas; sobram 14h a 17h.
        assertEquals(List.of(LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0), LocalTime.of(17, 0)),
                dia10.horariosLivres());
        assertEquals(AgendaService.Situacao.LOTADO, dias.get(10).situacao());
        assertEquals(AgendaService.Situacao.FECHADO, dias.get(11).situacao());
    }

    @Test
    void diaDeHojeSoOfereceHorariosFuturos() {
        abrir(AGORA.toLocalDate(), 8, 12);

        AgendaService.Dia hoje = agenda.mes(YearMonth.of(2026, 12), null).get(0);

        assertEquals(List.of(LocalTime.of(10, 0), LocalTime.of(11, 0)), hoje.horariosLivres());
    }

    @Test
    void atendimentoNaNoiteAnteriorBloqueiaOInicioDoDia() {
        LocalDate anterior = DIA.minusDays(1);
        abrir(anterior, 8, 23);
        marcar(3L, anterior.atTime(22, 0)); // bloqueia até 02h do dia 10
        abrir(DIA, 0, 6);

        AgendaService.Dia dia = agenda.mes(YearMonth.of(2026, 12), null).get(9);

        assertEquals(List.of(LocalTime.of(2, 0), LocalTime.of(3, 0), LocalTime.of(4, 0), LocalTime.of(5, 0)),
                dia.horariosLivres());
    }

    // ---- gestão dos dias ------------------------------------------------------------

    @Test
    void naoFechaDiaComAtendimentoMarcado() {
        marcar(1L, DIA.atTime(10, 0));

        assertThrows(RegraNegocioException.class, () -> agenda.fecharDia(DIA));
        verify(diaRepository, never()).delete(any());
    }

    @Test
    void fechaDiaSemAtendimentos() {
        agenda.fecharDia(DIA);
        verify(diaRepository).delete(any(DiaAtendimento.class));
    }

    @Test
    void naoAbreDiaNoPassadoNemJanelaInvalida() {
        assertThrows(RegraNegocioException.class,
                () -> agenda.abrirDias(List.of(AGORA.toLocalDate().minusDays(1)), LocalTime.of(8, 0), LocalTime.of(18, 0)));
        assertThrows(RegraNegocioException.class,
                () -> agenda.abrirDias(List.of(DIA.plusDays(3)), LocalTime.of(10, 0), LocalTime.of(10, 30)));
    }

    @Test
    void naoEncolheJanelaDeixandoAtendimentoDeFora() {
        marcar(1L, DIA.atTime(16, 0));

        assertThrows(RegraNegocioException.class,
                () -> agenda.abrirDias(List.of(DIA), LocalTime.of(8, 0), LocalTime.of(12, 0)));
    }

    @Test
    void abreVariosDiasComJanelaPadrao() {
        when(diaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        LocalDate a = LocalDate.of(2026, 12, 14);
        LocalDate b = LocalDate.of(2026, 12, 15);

        List<DiaAtendimento> abertos = agenda.abrirDias(List.of(b, a, a), null, null);

        assertEquals(2, abertos.size());
        assertEquals(a, abertos.get(0).getData());
        assertEquals(AgendaService.INICIO_PADRAO, abertos.get(0).getHoraInicio());
        assertEquals(AgendaService.FIM_PADRAO, abertos.get(1).getHoraFim());
    }

    // ---- apoio ------------------------------------------------------------------------

    private final List<DiaAtendimento> diasAbertos = new ArrayList<>();

    private void abrir(LocalDate data, int inicio, int fim) {
        diasAbertos.removeIf(d -> d.getData().equals(data));
        DiaAtendimento d = new DiaAtendimento();
        d.setTenantId(TENANT);
        d.setData(data);
        d.setHoraInicio(LocalTime.of(inicio, 0));
        d.setHoraFim(LocalTime.of(fim, 0));
        diasAbertos.add(d);

        when(diaRepository.findByTenantIdAndData(eq(TENANT), any())).thenAnswer(inv ->
                diasAbertos.stream().filter(x -> x.getData().equals(inv.getArgument(1))).findFirst());
        when(diaRepository.findByTenantIdAndDataBetweenOrderByData(eq(TENANT), any(), any())).thenAnswer(inv -> entre(inv.getArgument(1), inv.getArgument(2)));
        when(diaRepository.findForUpdateByTenantIdAndDataBetweenOrderByData(eq(TENANT), any(), any())).thenAnswer(inv -> entre(inv.getArgument(1), inv.getArgument(2)));
    }

    private List<DiaAtendimento> entre(LocalDate de, LocalDate ate) {
        return diasAbertos.stream().filter(d -> !d.getData().isBefore(de) && !d.getData().isAfter(ate)).toList();
    }

    private void marcar(Long id, LocalDateTime quando) {
        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOS.PENDENTE);
        os.setDataAgendada(quando);
        marcadas.add(os);
    }
}
