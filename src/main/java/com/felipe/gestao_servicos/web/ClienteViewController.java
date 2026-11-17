package com.felipe.gestao_servicos.web;

import com.felipe.gestao_servicos.domain.Cliente;
import com.felipe.gestao_servicos.dto.request.ClienteRequest;
import com.felipe.gestao_servicos.service.AcessoPortalService;
import com.felipe.gestao_servicos.service.ClienteService;
import com.felipe.gestao_servicos.service.RegraNegocioException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clientes")
public class ClienteViewController {
    private final ClienteService service;
    private final AcessoPortalService acessoPortalService;

    public ClienteViewController(ClienteService service, AcessoPortalService acessoPortalService) {
        this.service = service;
        this.acessoPortalService = acessoPortalService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", service.listar());
        return "clientes/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/form";
    }

    @PostMapping
    public String criar(@ModelAttribute ClienteRequest cliente) {
        service.salvar(cliente);
        return "redirect:/clientes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", service.buscarPorId(id));
        return "clientes/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute ClienteRequest cliente) {
        service.atualizar(id, cliente);
        return "redirect:/clientes";
    }

    /** Liberação do acesso do cliente ao portal. */
    @GetMapping("/{id}/acesso")
    public String acesso(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", acessoPortalService.cliente(id));
        model.addAttribute("acesso", acessoPortalService.acessoExistente(id).orElse(null));
        model.addAttribute("senhaMinima", AcessoPortalService.SENHA_MINIMA);
        return "clientes/acesso";
    }

    @PostMapping("/{id}/acesso")
    public String definirAcesso(@PathVariable Long id, @RequestParam String senha, RedirectAttributes flash) {
        try {
            acessoPortalService.definirSenha(id, senha);
            flash.addFlashAttribute("sucesso", "Acesso ao portal liberado. Envie a senha ao cliente por um canal seguro.");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/clientes/" + id + "/acesso";
    }

    @PostMapping("/{id}/acesso/revogar")
    public String revogarAcesso(@PathVariable Long id, RedirectAttributes flash) {
        acessoPortalService.revogar(id);
        flash.addFlashAttribute("sucesso", "Acesso ao portal revogado.");
        return "redirect:/clientes/" + id + "/acesso";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        service.excluir(id);
        return "redirect:/clientes";
    }
}