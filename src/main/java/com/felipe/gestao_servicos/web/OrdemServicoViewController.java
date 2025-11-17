package com.felipe.gestao_servicos.web;

import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.enums.StatusOS;
import com.felipe.gestao_servicos.service.ClienteService;
import com.felipe.gestao_servicos.service.OrdemServicoService;
import com.felipe.gestao_servicos.service.ServicoService;
import com.felipe.gestao_servicos.service.TecnicoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/os")
public class OrdemServicoViewController {
    private final OrdemServicoService service;
    private final ClienteService clienteService;
    private final TecnicoService tecnicoService;
    private final ServicoService servicoService;

    public OrdemServicoViewController(OrdemServicoService service, ClienteService clienteService, TecnicoService tecnicoService, ServicoService servicoService) {
        this.service = service;
        this.clienteService = clienteService;
        this.tecnicoService = tecnicoService;
        this.servicoService = servicoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ordens", service.listar());
        model.addAttribute("StatusOS", StatusOS.values());
        return "os/list";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("ordem", new OrdemServico());
        model.addAttribute("clientes", clienteService.listar());
        model.addAttribute("tecnicos", tecnicoService.listar());
        model.addAttribute("servicos", servicoService.listar());
        return "os/form";
    }

    @PostMapping
    public String criar(@ModelAttribute OrdemServico ordem) {
        service.salvar(ordem);
        return "redirect:/os";
    }

    @PostMapping("/{id}/status")
    public String atualizarStatus(@PathVariable Long id, @RequestParam("status") StatusOS status) {
        service.atualizarStatus(id, status);
        return "redirect:/os";
    }
}