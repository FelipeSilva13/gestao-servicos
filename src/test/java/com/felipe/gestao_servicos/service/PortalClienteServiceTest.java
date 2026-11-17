package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantUserDetails;
import com.felipe.gestao_servicos.domain.Avaliacao;
import com.felipe.gestao_servicos.domain.Cliente;
import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.domain.Tecnico;
import com.felipe.gestao_servicos.domain.Tenant;
import com.felipe.gestao_servicos.enums.StatusOS;
import com.felipe.gestao_servicos.repository.AvaliacaoRepository;
import com.felipe.gestao_servicos.repository.ClienteRepository;
import com.felipe.gestao_servicos.repository.OrdemServicoRepository;
import com.felipe.gestao_servicos.repository.ServicoRepository;
import com.felipe.gestao_servicos.repository.TenantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortalClienteServiceTest {

    private static final Long TENANT = 1L;
    private static final Long CLIENTE = 10L;

    @Mock OrdemServicoRepository ordemRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock ServicoRepository servicoRepository;
    @Mock TenantRepository tenantRepository;
    @Mock AvaliacaoRepository avaliacaoRepository;
    @Mock AnexoService anexoService;
    @Mock EventoOrdemService eventos;

    PortalClienteService service;
    Tenant tenant;
    Cliente cliente;

    @BeforeEach
    void setUp() {
        service = new PortalClienteService(ordemRepository, clienteRepository, servicoRepository,
                tenantRepository, avaliacaoRepository, anexoService, eventos);
        tenant = new Tenant();
        tenant.setId(TENANT);
        cliente = new Cliente();
        cliente.setId(CLIENTE);
        cliente.setNome("Maria");

        TenantUserDetails usuario = new TenantUserDetails("maria@cliente.com", "x",
                List.of(new SimpleGrantedAuthority(UsuarioAtual.ROLE_CLIENTE)), TENANT, 5L, CLIENTE);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));

        lenient().when(ordemRepository.save(any(OrdemServico.class))).thenAnswer(i -> i.getArgument(0));
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    private OrdemServico ordem(StatusOS status) {
        OrdemServico os = new OrdemServico();
        os.setId(7L);
        os.setTenant(tenant);
        os.setCliente(cliente);
        os.setStatus(status);
        when(ordemRepository.findByIdAndCliente_IdAndTenant_Id(7L, CLIENTE, TENANT)).thenReturn(Optional.of(os));
        return os;
    }

    @Test
    void ordemDeOutroClienteNaoEEncontrada() {
        when(ordemRepository.findByIdAndCliente_IdAndTenant_Id(99L, CLIENTE, TENANT)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.minhaOrdem(99L));
    }

    @Test
    void solicitarCriaOrdemSolicitadaSemTecnicoENotificaEquipe() {
        when(clienteRepository.findByIdAndTenant_Id(CLIENTE, TENANT)).thenReturn(Optional.of(cliente));
        when(tenantRepository.findById(TENANT)).thenReturn(Optional.of(tenant));
        LocalDateTime preferida = LocalDateTime.now().plusDays(3);

        OrdemServico os = service.solicitar("  Portão travado  ", null, preferida, List.of());

        assertEquals(StatusOS.SOLICITADA, os.getStatus());
        assertEquals("Portão travado", os.getDescricaoProblema());
        assertNull(os.getTecnico());
        assertEquals(preferida, os.getDataAgendada());
        assertFalse(os.isAgendamentoConfirmado());
        verify(eventos).registrar(eq(os), anyString(), eq(EventoOrdemService.Destino.EQUIPE));
    }

    @Test
    void solicitarRejeitaDataNoPassado() {
        assertThrows(RegraNegocioException.class,
                () -> service.solicitar("Algo", null, LocalDateTime.now().minusDays(1), List.of()));
        verify(ordemRepository, never()).save(any());
    }

    @Test
    void aprovarOrcamentoLiberaExecucaoComValorDoOrcamento() {
        OrdemServico os = ordem(StatusOS.AGUARDANDO_APROVACAO);
        os.setOrcamentoValor(new BigDecimal("420.00"));

        service.aprovarOrcamento(7L);

        assertEquals(StatusOS.PENDENTE, os.getStatus());
        assertEquals(new BigDecimal("420.00"), os.getValorTotal());
        assertNotNull(os.getOrcamentoRespondidoEm());
        verify(eventos).registrar(eq(os), anyString(), eq(EventoOrdemService.Destino.EQUIPE));
    }

    @Test
    void naoAprovaSemOrcamentoPendente() {
        ordem(StatusOS.SOLICITADA);
        assertThrows(RegraNegocioException.class, () -> service.aprovarOrcamento(7L));
    }

    @Test
    void recusarOrcamentoGuardaMotivo() {
        OrdemServico os = ordem(StatusOS.AGUARDANDO_APROVACAO);
        os.setOrcamentoValor(BigDecimal.TEN);

        service.recusarOrcamento(7L, " Caro demais ");

        assertEquals(StatusOS.RECUSADA, os.getStatus());
        assertEquals("Caro demais", os.getOrcamentoMotivoRecusa());
    }

    @Test
    void naoCancelaOrdemEmExecucao() {
        ordem(StatusOS.EM_EXECUCAO);
        assertThrows(RegraNegocioException.class, () -> service.cancelar(7L));
    }

    @Test
    void reagendarVoltaParaAConfirmar() {
        OrdemServico os = ordem(StatusOS.PENDENTE);
        os.setAgendamentoConfirmado(true);
        LocalDateTime nova = LocalDateTime.now().plusDays(5);

        service.reagendar(7L, nova);

        assertEquals(nova, os.getDataAgendada());
        assertFalse(os.isAgendamentoConfirmado());
    }

    @Test
    void avaliacaoSoAposConclusao() {
        ordem(StatusOS.EM_EXECUCAO);
        assertThrows(RegraNegocioException.class, () -> service.avaliar(7L, 5, 5, null));
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void avaliacaoUnicaEComNotasValidas() {
        OrdemServico os = ordem(StatusOS.FINALIZADA);
        Tecnico tecnico = new Tecnico();
        tecnico.setId(3L);
        os.setTecnico(tecnico);
        when(avaliacaoRepository.existsByOrdemIdAndTenantId(7L, TENANT)).thenReturn(false);
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(RegraNegocioException.class, () -> service.avaliar(7L, 6, 5, null));
        Avaliacao a = service.avaliar(7L, 5, 4, "Muito bom");
        assertEquals(5, a.getNotaServico());
        assertEquals(4, a.getNotaTecnico());

        when(avaliacaoRepository.existsByOrdemIdAndTenantId(7L, TENANT)).thenReturn(true);
        assertThrows(RegraNegocioException.class, () -> service.avaliar(7L, 5, 5, null));
    }
}
