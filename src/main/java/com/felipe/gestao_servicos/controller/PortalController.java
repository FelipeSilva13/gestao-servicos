package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.service.EventoOrdemService;
import com.felipe.gestao_servicos.service.PortalClienteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Endpoints JSON do portal do cliente. */
@RestController
@RequestMapping("/portal")
public class PortalController {

    private final PortalClienteService portal;
    private final EventoOrdemService eventos;

    public PortalController(PortalClienteService portal, EventoOrdemService eventos) {
        this.portal = portal;
        this.eventos = eventos;
    }

    /** Consultado periodicamente pela página de detalhe para atualizar em tempo real. */
    @GetMapping("/ordens/{id}/estado")
    public Map<String, String> estado(@PathVariable Long id) {
        OrdemServico os = portal.minhaOrdem(id);
        return Map.of("versao", eventos.versao(os), "status", os.getStatus().getLabel());
    }
}
