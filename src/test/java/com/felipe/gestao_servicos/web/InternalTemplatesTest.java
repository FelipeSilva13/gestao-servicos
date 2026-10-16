package com.felipe.gestao_servicos.web;

import com.felipe.gestao_servicos.domain.Cliente;
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

import java.util.List;
import java.util.Locale;
import java.util.Map;

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