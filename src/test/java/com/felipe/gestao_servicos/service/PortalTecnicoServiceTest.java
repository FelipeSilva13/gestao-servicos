package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantUserDetails;
import com.felipe.gestao_servicos.domain.Cliente;
import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.domain.Tecnico;
import com.felipe.gestao_servicos.domain.Tenant;
import com.felipe.gestao_servicos.enums.RespostaTecnico;
import com.felipe.gestao_servicos.enums.StatusOS;
import com.felipe.gestao_servicos.repository.OrdemServicoRepository;
import com.felipe.gestao_servicos.repository.TecnicoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortalTecnicoServiceTest {

    private static final Long TENANT = 1L;
    private static final Long TECNICO = 20L;

    @Mock OrdemServicoRepository ordemRepository;
    @Mock TecnicoRepository tecnicoRepository;
    @Mock EventoOrdemService eventos;

    PortalTecnicoService service;
    Tecnico tecnico;

    @BeforeEach
    void setUp() {
        service = new PortalTecnicoService(ordemRepository, tecnicoRepository, eventos);
        tecnico = new Tecnico();
        tecnico.setId(TECNICO);
        tecnico.setNome("Carlos");

        TenantUserDetails usuario = new TenantUserDetails("carlos@empresa.com", "x",
                List.of(new SimpleGrantedAuthority(UsuarioAtual.ROLE_TECNICO)), TENANT, 6L, null, TECNICO);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));

        lenient().when(ordemRepository.save(any(OrdemServico.class))).thenAnswer(i -> i.getArgument(0));
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    private OrdemServico ordem(StatusOS status, RespostaTecnico resposta) {
        Tenant tenant = new Tenant();
        tenant.setId(TENANT);
        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNome("Maria");
        OrdemServico os = new OrdemServico();
        os.setId(7L);
        os.setTenant(tenant);
        os.setCliente(cliente);
        os.setTecnico(tecnico);
        os.setStatus(status);
        os.setTecnicoResposta(resposta);
        when(ordemRepository.findByIdAndTecnico_IdAndTenant_Id(7L, TECNICO, TENANT)).thenReturn(Optional.of(os));
        return os;
    }

    @Test
    void aceitarRegistraRespostaEAvisaEquipe() {
        ordem(StatusOS.PENDENTE, RespostaTecnico.PENDENTE);

        OrdemServico salvo = service.aceitar(7L);

        assertEquals(RespostaTecnico.ACEITA, salvo.getTecnicoResposta());
        assertNotNull(salvo.getTecnicoRespondidoEm());
        verify(eventos).registrar(eq(salvo), contains("confirmou"), eq(EventoOrdemService.Destino.EQUIPE));
    }

    @Test
    void recusarGuardaMotivoSemExporAoCliente() {
        ordem(StatusOS.PENDENTE, RespostaTecnico.PENDENTE);

        OrdemServico salvo = service.recusar(7L, "  Estarei viajando  ");

        assertEquals(RespostaTecnico.RECUSADA, salvo.getTecnicoResposta());
        assertEquals("Estarei viajando", salvo.getTecnicoMotivoRecusa());
        verify(eventos).avisarEquipe(eq(salvo), contains("Estarei viajando"));
        verify(eventos, never()).registrar(any(), any(), any());
    }

    @Test
    void naoPermiteResponderDuasVezes() {
        ordem(StatusOS.PENDENTE, RespostaTecnico.ACEITA);

        assertThrows(RegraNegocioException.class, () -> service.recusar(7L, null));
        verify(ordemRepository, never()).save(any());
    }

    @Test
    void naoPermiteResponderOrdemEncerrada() {
        ordem(StatusOS.CANCELADA, RespostaTecnico.PENDENTE);

        assertThrows(RegraNegocioException.class, () -> service.aceitar(7L));
    }

    @Test
    void ordemDeOutroTecnicoNaoEncontrada() {
        when(ordemRepository.findByIdAndTecnico_IdAndTenant_Id(99L, TECNICO, TENANT)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.aceitar(99L));
    }

    @Test
    void clienteNaoUsaAreaDoTecnico() {
        TenantUserDetails cliente = new TenantUserDetails("maria@cliente.com", "x",
                List.of(new SimpleGrantedAuthority(UsuarioAtual.ROLE_CLIENTE)), TENANT, 5L, 10L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cliente, null, cliente.getAuthorities()));

        assertThrows(IllegalStateException.class, () -> service.minhasOrdens());
    }
}
