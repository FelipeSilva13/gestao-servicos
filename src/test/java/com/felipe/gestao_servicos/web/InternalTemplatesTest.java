package com.felipe.gestao_servicos.web;

import com.felipe.gestao_servicos.domain.Anexo;
import com.felipe.gestao_servicos.domain.Avaliacao;
import com.felipe.gestao_servicos.domain.Cliente;
import com.felipe.gestao_servicos.domain.Notificacao;
import com.felipe.gestao_servicos.domain.OrdemHistorico;
import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.domain.Servico;
import com.felipe.gestao_servicos.domain.Tecnico;
import com.felipe.gestao_servicos.enums.StatusOS;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalTemplatesTest {

    private SpringTemplateEngine templateEngine;
    private ServletContext servletContext;
    private JakartaServletWebApplication webApplication;

    @BeforeEach
    void configurarTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(false);

        templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        servletContext = new MockServletContext();
        webApplication = JakartaServletWebApplication.buildApplication(servletContext);
    }

    @Test
    void paginasInternasRenderizamComSeusModelos() {
        assertRenderiza("index", Map.of(
                "totalTecnicos", 2L,
                "totalClientes", 3L,
                "totalServicos", 3L,
                "totalOrdens", 1L,
                "ordensRecentes", List.of()));
        assertRenderiza("tecnicos/list", Map.of("tecnicos", List.of()));
        assertRenderiza("tecnicos/form", Map.of("tecnico", new Tecnico()));
        assertRenderiza("clientes/list", Map.of("clientes", List.of()));
        assertRenderiza("clientes/form", Map.of("cliente", new Cliente()));
        assertRenderiza("servicos/list", Map.of("servicos", List.of()));
        assertRenderiza("servicos/form", Map.of("servico", new Servico()));
        assertRenderiza("os/list", Map.of("ordens", List.of(), "StatusOS", StatusOS.values()));
        assertRenderiza("os/form", Map.of(
                "clientes", List.of(),
                "tecnicos", List.of(),
                "servicos", List.of(),
                "StatusOS", StatusOS.values()));
    }

    @Test
    void paginasDoPortalEDetalheRenderizamEmCadaEtapa() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Maria");
        cliente.setEmail("maria@cliente.com");

        Tecnico tecnico = new Tecnico();
        tecnico.setId(3L);
        tecnico.setNome("Carlos");

        OrdemHistorico evento = new OrdemHistorico();
        evento.setDescricao("Nova solicitação aberta");
        evento.setStatus(StatusOS.SOLICITADA);

        Anexo foto = new Anexo();
        foto.setId(9L);
        foto.setNomeOriginal("vazamento.jpg");
        foto.setContentType("image/jpeg");
        foto.setTamanho(2048L);
        Anexo pdf = new Anexo();
        pdf.setId(10L);
        pdf.setNomeOriginal("nota.pdf");
        pdf.setContentType("application/pdf");
        pdf.setTamanho(4_000_000L);

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setNotaServico(5);
        avaliacao.setNotaTecnico(4);
        avaliacao.setComentario("Ótimo");

        for (StatusOS status : StatusOS.values()) {
            OrdemServico os = new OrdemServico();
            os.setId(7L);
            os.setCliente(cliente);
            os.setStatus(status);
            os.setDescricaoProblema("Ar-condicionado pingando");
            os.setDataAgendada(LocalDateTime.now().plusDays(2));
            if (status != StatusOS.SOLICITADA) {
                os.setTecnico(tecnico);
                os.setOrcamentoValor(new BigDecimal("350.00"));
                os.setOrcamentoDescricao("Troca do dreno");
                os.setOrcamentoEnviadoEm(LocalDateTime.now());
            }

            Map<String, Object> detalhe = new HashMap<>();
            detalhe.put("ordem", os);
            detalhe.put("historico", List.of(evento));
            detalhe.put("anexos", List.of(foto, pdf));
            detalhe.put("avaliacao", status == StatusOS.FINALIZADA ? avaliacao : null);
            detalhe.put("versao", status + ":1");
            detalhe.put("extensoesAceitas", ".jpg,.pdf");
            detalhe.put("notificacoesNaoLidas", 3L);
            assertRenderiza("portal/ordem", detalhe);

            detalhe.put("avaliacao", null);
            assertRenderiza("portal/ordem", detalhe);

            detalhe.put("StatusOS", StatusOS.values());
            detalhe.put("tecnicos", List.of());
            detalhe.put("servicos", List.of());
            assertRenderiza("os/detalhe", detalhe);
        }

        OrdemServico aberta = new OrdemServico();
        aberta.setId(8L);
        aberta.setCliente(cliente);
        aberta.setStatus(StatusOS.FINALIZADA);
        assertRenderiza("portal/painel", Map.of(
                "cliente", cliente,
                "ordens", List.of(aberta),
                "filtro", "todas",
                "totalAndamento", 0L,
                "totalConcluidas", 1L,
                "totalOrcamentos", 0L,
                "avaliadas", Set.of(),
                "totalGeral", 1));
        assertRenderiza("portal/solicitacao", Map.of("servicos", List.of(), "extensoesAceitas", ".jpg"));

        Notificacao notificacao = new Notificacao();
        notificacao.setId(1L);
        notificacao.setMensagem("OS-007: Orçamento enviado");
        assertRenderiza("notificacoes/list", Map.of("notificacoes", List.of(notificacao), "isCliente", true));
        assertRenderiza("notificacoes/list", Map.of("notificacoes", List.of(), "isCliente", false));

        assertRenderiza("clientes/acesso", Map.of("cliente", cliente, "senhaMinima", 8));
    }

    @Test
    void contadorDeNotificacoesApareceNoSino() {
        WebContext context = new WebContext(
                webApplication.buildExchange(new MockHttpServletRequest(servletContext), new MockHttpServletResponse()),
                Locale.forLanguageTag("pt-BR"));
        context.setVariable("tecnicos", List.of());
        context.setVariable("notificacoesNaoLidas", 4L);
        String html = templateEngine.process("tecnicos/list", context);
        assertTrue(html.contains(">4</span>"), "o sino deve mostrar a quantidade de não lidas");
    }

    private void assertRenderiza(String template, Map<String, Object> variables) {
        WebContext context = new WebContext(
            webApplication.buildExchange(
                new MockHttpServletRequest(servletContext),
                new MockHttpServletResponse()),
                Locale.forLanguageTag("pt-BR"));
        variables.forEach(context::setVariable);

        String html = assertDoesNotThrow(() -> templateEngine.process(template, context), template);
        assertFalse(html.isBlank(), template + " deve produzir HTML");
        assertTrue(html.contains("class=\"menu\""), template + " deve usar a navegação compartilhada");
        assertTrue(html.contains("class=\"topbar\""), template + " deve usar a barra superior compartilhada");
        assertTrue(html.contains("name=\"viewport\""), template + " deve declarar viewport móvel");
        assertEquals(1, html.split("<!DOCTYPE html>", -1).length - 1, template + " deve conter um único documento HTML");
    }
}