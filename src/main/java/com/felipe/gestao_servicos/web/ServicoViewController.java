package com.felipe.gestao_servicos.web;

import com.felipe.gestao_servicos.domain.Servico;
import com.felipe.gestao_servicos.service.ServicoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/servicos")
public class ServicoViewController {
    private final ServicoService service;

    public ServicoViewController(ServicoService service) { this.service = service; }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("servicos", service.listar());
        return "servicos/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("servico", new Servico());
        return "servicos/form";
    }

    @PostMapping
    public String criar(@ModelAttribute Servico servico) {
        service.salvar(servico);
        return "redirect:/servicos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("servico", service.obter(id));
        return "servicos/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute Servico servico) {
        servico.setId(id);
        service.salvar(servico);
        return "redirect:/servicos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        service.excluir(id);
        return "redirect:/servicos";
    }
}