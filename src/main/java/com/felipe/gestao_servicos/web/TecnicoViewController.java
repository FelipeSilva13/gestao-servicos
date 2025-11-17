package com.felipe.gestao_servicos.web;

import com.felipe.gestao_servicos.domain.Tecnico;
import com.felipe.gestao_servicos.service.TecnicoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tecnicos")
public class TecnicoViewController {
    private final TecnicoService service;

    public TecnicoViewController(TecnicoService service) { this.service = service; }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tecnicos", service.listar());
        return "tecnicos/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("tecnico", new Tecnico());
        return "tecnicos/form";
    }

    @PostMapping
    public String criar(@ModelAttribute Tecnico tecnico) {
        service.salvar(tecnico);
        return "redirect:/tecnicos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("tecnico", service.obter(id));
        return "tecnicos/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute Tecnico tecnico) {
        tecnico.setId(id);
        service.salvar(tecnico);
        return "redirect:/tecnicos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        service.excluir(id);
        return "redirect:/tecnicos";
    }
}