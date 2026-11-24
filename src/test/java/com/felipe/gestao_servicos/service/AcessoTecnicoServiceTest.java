package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantContext;
import com.felipe.gestao_servicos.domain.Tecnico;
import com.felipe.gestao_servicos.domain.Tenant;
import com.felipe.gestao_servicos.domain.Usuario;
import com.felipe.gestao_servicos.repository.TecnicoRepository;
import com.felipe.gestao_servicos.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcessoTecnicoServiceTest {

    private static final Long TENANT = 1L;

    @Mock TecnicoRepository tecnicoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;

    AcessoTecnicoService service;
    Tecnico tecnico;

    @BeforeEach
    void setUp() {
        service = new AcessoTecnicoService(tecnicoRepository, usuarioRepository, passwordEncoder);
        TenantContext.setCurrentTenant(TENANT);
        Tenant tenant = new Tenant();
        tenant.setId(TENANT);
        tecnico = new Tecnico();
        tecnico.setId(20L);
        tecnico.setNome("Carlos");
        tecnico.setEmail("carlos@empresa.com");
        tecnico.setTenant(tenant);
        lenient().when(tecnicoRepository.findByIdAndTenant_Id(20L, TENANT)).thenReturn(Optional.of(tecnico));
        lenient().when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(passwordEncoder.encode(any())).thenReturn("hash");
    }

    @AfterEach
    void limpar() {
        TenantContext.clear();
    }

    @Test
    void criaUsuarioComPapelDeTecnico() {
        when(usuarioRepository.findByTecnico_IdAndTenant_Id(20L, TENANT)).thenReturn(List.of());
        when(usuarioRepository.findByEmail("carlos@empresa.com")).thenReturn(Optional.empty());

        Usuario usuario = service.definirSenha(20L, "senhaSegura1");

        assertEquals(UsuarioAtual.ROLE_TECNICO, usuario.getRole());
        assertSame(tecnico, usuario.getTecnico());
        assertNull(usuario.getCliente());
        assertEquals("hash", usuario.getSenha());
    }

    @Test
    void exigeSenhaMinima() {
        assertThrows(RegraNegocioException.class, () -> service.definirSenha(20L, "curta"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void exigeEmailDoTecnico() {
        tecnico.setEmail(null);
        assertThrows(RegraNegocioException.class, () -> service.definirSenha(20L, "senhaSegura1"));
    }

    @Test
    void naoReaproveitaEmailDeOutroUsuario() {
        when(usuarioRepository.findByTecnico_IdAndTenant_Id(20L, TENANT)).thenReturn(List.of());
        when(usuarioRepository.findByEmail("carlos@empresa.com")).thenReturn(Optional.of(new Usuario()));

        assertThrows(RegraNegocioException.class, () -> service.definirSenha(20L, "senhaSegura1"));
    }
}
