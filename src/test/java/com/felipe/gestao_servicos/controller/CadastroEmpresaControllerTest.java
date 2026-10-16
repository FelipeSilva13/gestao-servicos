package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.dto.request.CadastroEmpresaRequest;
import com.felipe.gestao_servicos.dto.response.CadastroEmpresaResponse;
import com.felipe.gestao_servicos.service.CadastroEmpresaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class CadastroEmpresaControllerTest {

    @Mock
    private CadastroEmpresaService cadastroEmpresaService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarController() {
        mockMvc = standaloneSetup(new CadastroEmpresaController(cadastroEmpresaService)).build();
    }

    @Test
    void deveCriarEmpresaEDevolverResponseSemSenha() throws Exception {
        when(cadastroEmpresaService.cadastrar(any(CadastroEmpresaRequest.class)))
                .thenReturn(new CadastroEmpresaResponse(7L, "Empresa Teste", "admin@empresa.com"));

        mockMvc.perform(post("/api/empresas")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nomeEmpresa": "Empresa Teste",
                                  "email": "admin@empresa.com",
                                  "senha": "senha-segura"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nomeEmpresa").value("Empresa Teste"))
                .andExpect(jsonPath("$.emailAdministrador").value("admin@empresa.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void deveResponderConflitoQuandoCadastroForDuplicado() throws Exception {
        when(cadastroEmpresaService.cadastrar(any(CadastroEmpresaRequest.class)))
                .thenThrow(new IllegalArgumentException("E-mail já está em uso."));

        mockMvc.perform(post("/api/empresas")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nomeEmpresa": "Empresa Teste",
                                  "email": "admin@empresa.com",
                                  "senha": "senha-segura"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("E-mail já está em uso."));
    }
}