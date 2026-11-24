package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.service.AgendaService;
import com.felipe.gestao_servicos.service.EventoOrdemService;
import com.felipe.gestao_servicos.service.PortalClienteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.Map;

/** Endpoints JSON do portal do cliente. */
@RestController
@RequestMapping("/portal")
public class PortalController {

    private final PortalClienteService portal;
    private final EventoOrdemService eventos;
    private final AgendaService agenda;

    public PortalController(PortalClienteService portal, EventoOrdemService eventos, AgendaService agenda) {
        this.portal = portal;
        this.eventos = eventos;
        this.agenda = agenda;
    }

    /** Consultado periodicamente pela página de detalhe para atualizar em tempo real. */
    @GetMapping("/ordens/{id}/estado")
    public Map<String, String> estado(@PathVariable Long id) {
        OrdemServico os = portal.minhaOrdem(id);
        return Map.of("versao", eventos.versao(os), "status", os.getStatus().getLabel());
    }

    /**
     * Dias e horários livres do mês para o calendário do cliente. Não revela quem marcou nem quando:
     * só diz se o dia está disponível, lotado ou fechado.
     *
     * @param ordemId ordem do próprio cliente que está sendo remarcada (o horário atual dela fica livre)
     */
    @GetMapping("/agenda")
    public Map<String, Object> agenda(@RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes,
                                      @RequestParam(required = false) Long ordemId) {
        Long ignorar = ordemId == null ? null : portal.minhaOrdem(ordemId).getId();
        return AgendaJson.paraCliente(mes, agenda.mes(mes, ignorar));
    }
}
