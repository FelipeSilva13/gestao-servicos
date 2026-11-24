package com.felipe.gestao_servicos.web;

import com.felipe.gestao_servicos.service.AgendaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Tela da agenda da empresa: escolher os dias abertos e ver os atendimentos marcados. */
@Controller
public class AgendaViewController {

    @GetMapping("/agenda")
    public String agenda(Model model) {
        model.addAttribute("inicioPadrao", AgendaService.INICIO_PADRAO);
        model.addAttribute("fimPadrao", AgendaService.FIM_PADRAO);
        model.addAttribute("intervaloHoras", AgendaService.INTERVALO_MINIMO.toHours());
        return "agenda/calendario";
    }
}
