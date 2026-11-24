package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantContext;
import com.felipe.gestao_servicos.domain.Cliente;
import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.domain.Tecnico;
import com.felipe.gestao_servicos.domain.Tenant;
import com.felipe.gestao_servicos.enums.RespostaTecnico;
import com.felipe.gestao_servicos.enums.StatusOS;
import com.felipe.gestao_servicos.repository.ClienteRepository;
import com.felipe.gestao_servicos.repository.OrdemServicoRepository;
import com.felipe.gestao_servicos.repository.ServicoRepository;
import com.felipe.gestao_servicos.repository.TecnicoRepository;
import com.felipe.gestao_servicos.repository.TenantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Ao designar (ou trocar) o técnico, a ordem aguarda a resposta dele e ele é notificado. */
@ExtendWith(MockitoExtension.class)
class OrdemServicoDesignacaoTecnicoTest {

    private static final Long TENANT = 1L;

    @Mock OrdemServicoRepository repository;
    @Mock ServicoRepository servicoRepository;
    @Mock TecnicoRepository tecnicoRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock TenantRepository tenantRepository;
    @Mock EventoOrdemService eventos;
    @Mock AgendaService agenda;

    OrdemServicoService service;
    OrdemServico os;
    Tecnico carlos;
    Tecnico ana;

    @BeforeEach
    void setUp() {
        service = new OrdemServicoService(repository, servicoRepository, tecnicoRepository, clienteRepository,
                tenantRepository, eventos, agenda);
        TenantContext.setCurrentTenant(TENANT);
        Tenant tenant = new Tenant();
        tenant.setId(TENANT);
        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNome("Maria");
        carlos = tecnico(20L, "Carlos");
        ana = tecnico(21L, "Ana");

        os = new OrdemServico();
        os.setId(7L);
        os.setTenant(tenant);
        os.setCliente(cliente);
        os.setStatus(StatusOS.SOLICITADA);
        when(repository.findByIdAndTenant_Id(7L, TENANT)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(tecnicoRepository.findByIdAndTenant_Id(20L, TENANT)).thenReturn(Optional.of(carlos));
        lenient().when(tecnicoRepository.findByIdAndTenant_Id(21L, TENANT)).thenReturn(Optional.of(ana));
    }

    @AfterEach
    void limpar() {
        TenantContext.clear();
    }

    private static Tecnico tecnico(Long id, String nome) {
        Tecnico t = new Tecnico();
        t.setId(id);
        t.setNome(nome);
        return t;
    }

    @Test
    void designarTecnicoDeixaOrdemAguardandoRespostaENotifica() {
        service.atribuir(7L, 20L, null);

        assertEquals(RespostaTecnico.PENDENTE, os.getTecnicoResposta());
        verify(eventos).avisarTecnico(eq(os), contains("solicitou você"));
    }

    @Test
    void trocarTecnicoAposRecusaPedeNovaResposta() {
        os.setTecnico(carlos);
        os.setTecnicoResposta(RespostaTecnico.RECUSADA);
        os.setTecnicoMotivoRecusa("Viajando");
        os.setTecnicoRespondidoEm(LocalDateTime.now());

        service.atribuir(7L, 21L, null);

        assertSame(ana, os.getTecnico());
        assertEquals(RespostaTecnico.PENDENTE, os.getTecnicoResposta());
        assertNull(os.getTecnicoMotivoRecusa());
        assertNull(os.getTecnicoRespondidoEm());
        verify(eventos).avisarTecnico(eq(os), any());
    }

    @Test
    void manterMesmoTecnicoNaoReenviaSolicitacao() {
        os.setTecnico(carlos);
        os.setTecnicoResposta(RespostaTecnico.ACEITA);

        service.atribuir(7L, 20L, null);

        assertEquals(RespostaTecnico.ACEITA, os.getTecnicoResposta());
        verify(eventos, never()).avisarTecnico(any(), any());
    }

    @Test
    void removerTecnicoLimpaResposta() {
        os.setTecnico(carlos);
        os.setTecnicoResposta(RespostaTecnico.PENDENTE);

        service.atribuir(7L, null, null);

        assertNull(os.getTecnico());
        assertNull(os.getTecnicoResposta());
        verify(eventos, never()).avisarTecnico(any(), any());
    }
}
