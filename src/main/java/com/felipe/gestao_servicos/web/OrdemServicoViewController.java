package com.felipe.gestao_servicos.web;

import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.dto.request.OrdemRequest;
import com.felipe.gestao_servicos.enums.StatusOS;
import com.felipe.gestao_servicos.service.ClienteService;
import com.felipe.gestao_servicos.service.OrdemServicoService;
import com.felipe.gestao_servicos.service.ServicoService;
import com.felipe.gestao_servicos.service.TecnicoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
        model.addAttribute("ordens", service.listarEntidades());
        model.addAttribute("StatusOS", StatusOS.values());
        return "os/list";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("ordem", new OrdemServico());
        model.addAttribute("StatusOS", StatusOS.values());
        model.addAttribute("clientes", clienteService.listar());
        model.addAttribute("tecnicos", tecnicoService.listar());
        model.addAttribute("servicos", servicoService.listar());
        return "os/form";
    }

    @PostMapping
    public String criar(@ModelAttribute OrdemServico ordem) {
        if (ordem.getCliente() == null || ordem.getCliente().getId() == null
                || ordem.getTecnico() == null || ordem.getTecnico().getId() == null
                || ordem.getServico() == null || ordem.getServico().getId() == null) {
            return "redirect:/os/nova?error=Selecione cliente, técnico e serviço";
        }
        OrdemRequest request = new OrdemRequest(
                ordem.getCliente().getId(),
                ordem.getTecnico().getId(),
                ordem.getServico().getId(),
                ordem.getStatus() != null ? ordem.getStatus() : StatusOS.PENDENTE,
                LocalDateTime.now(),
                null,
                null,
                ordem.getObservacoes(),
                null
        );
        service.salvar(request);
        return "redirect:/os";
    }

    @PostMapping("/{id}/status")
    public String atualizarStatus(@PathVariable Long id, @RequestParam("status") StatusOS status) {
        service.atualizarStatus(id, status);
        return "redirect:/os";
    }
}