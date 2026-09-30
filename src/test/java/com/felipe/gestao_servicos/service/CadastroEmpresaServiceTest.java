package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.domain.Tenant;
import com.felipe.gestao_servicos.domain.Usuario;
import com.felipe.gestao_servicos.dto.request.CadastroEmpresaRequest;
import com.felipe.gestao_servicos.repository.TenantRepository;
import com.felipe.gestao_servicos.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CadastroEmpresaServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder authentication;

    @InjectMocks
    private CadastroEmpresaService cadastroEmpresaService;

    @Test
    public void deveCadastrarEmpresaEAdministradorComSucesso() {

        //Arrange
        CadastroEmpresaRequest request = new CadastroEmpresaRequest(
                "Empresa Teste",
                "empresa@gmail.com",
                "123456"
        );

        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setNome("Empresa Teste");

        when(tenantRepository.existsByNomeIgnoreCase("Empresa Teste"))
                .thenReturn(false);

        when(usuarioRepository.findByEmail("empresa@gmail.com"))
                .thenReturn(Optional.empty());

        when(tenantRepository.save(any(Tenant.class)))
                .thenReturn(tenant);

        when(authentication.encode("123456"))
                .thenReturn("senah-criptografada");

        //Act
        cadastroEmpresaService.cadastrar(request);

        //Assert
        verify(tenantRepository).save(any(Tenant.class));

        verify(authentication).encode("123456");

        verify(usuarioRepository).save(any(Usuario.class));

    }

    @Test
    void deveRecusarCadastroQuandoEmpresaExiste(){

        //Arrange
        CadastroEmpresaRequest request = new CadastroEmpresaRequest(
                "Empresa Existente",
                "admin@email.com",
                "123456"
        );

        when(tenantRepository.existsByNomeIgnoreCase("Empresa Existente"))
                .thenReturn(true);

        //Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                ()-> cadastroEmpresaService.cadastrar(request));

        assertEquals("Já existe uma empresa com este nome.", exception.getMessage());

        verify(tenantRepository, never()).save(any(Tenant.class));
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(authentication, never()).encode(anyString());

    }

    @Test
    void deveNormalizarNomeEEmailAntesDeSalvar(){

        //Arrange
        CadastroEmpresaRequest request = new CadastroEmpresaRequest(
                "Minha Empresa",
                "ADMIN@EMAIL.COM",
                "123456"
        );

        Tenant tenant = new Tenant();
        tenant.setId(10L);
        tenant.setNome("Minha Empresa");

        when(tenantRepository.existsByNomeIgnoreCase("Minha Empresa"))
                .thenReturn(false);
        when(usuarioRepository.findByEmail("admin@email.com"))
                .thenReturn(Optional.empty());
        when(tenantRepository.save(any(Tenant.class)))
                .thenReturn(tenant);
        when(authentication.encode("123456"))
                .thenReturn("senah-criptografada");

        //Act
        cadastroEmpresaService.cadastrar(request);

        //Assert
        ArgumentCaptor<Tenant> captor =
                ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());

        Tenant tenantSalvo = captor.getValue();

        assertEquals("Minha Empresa", tenantSalvo.getNome());

        ArgumentCaptor<Usuario> usuarioCaptor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario usuarioSalvo = usuarioCaptor.getValue();

        assertEquals("admin@email.com", usuarioSalvo.getEmail());
    }

    @Test
    void deveCriarUsuarioComRoleAdmin(){

        //Arrange
        CadastroEmpresaRequest request = new CadastroEmpresaRequest(
                "Empresa Teste",
                "email@email.com",
                "123456");

        Tenant tenant = new Tenant();
        tenant.setId(1L);

        when(tenantRepository.existsByNomeIgnoreCase("Empresa Teste"))
                .thenReturn(false);

        when(usuarioRepository.findByEmail("email@email.com"))
                .thenReturn(Optional.empty());

        when(tenantRepository.save(any(Tenant.class)))
                .thenReturn(tenant);

        when(authentication.encode("123456"))
                .thenReturn("senah-criptografada");

        //Act
        cadastroEmpresaService.cadastrar(request);

        //Assert
        ArgumentCaptor<Usuario> usuarioArgumentCaptor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(usuarioArgumentCaptor.capture());
        Usuario usuarioSalvo = usuarioArgumentCaptor.getValue();
        assertEquals("ROLE_ADMIN", usuarioSalvo.getRole());

    }

    @Test
    void deveAssociarAdministradorAoTenantCriado(){

        //Arrange
    }
}
