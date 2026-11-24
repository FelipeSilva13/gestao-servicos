package com.felipe.gestao_servicos.web;

import com.felipe.gestao_servicos.service.NotificacaoService;
import com.felipe.gestao_servicos.service.UsuarioAtual;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** Central de notificações, compartilhada entre equipe e clientes do portal. */
@Controller
@RequestMapping("/notificacoes")
public class NotificacaoViewController {

    private final NotificacaoService service;

    public NotificacaoViewController(NotificacaoService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("notificacoes", service.listarDoUsuarioAtual());
        model.addAttribute("isCliente", UsuarioAtual.isCliente());
        model.addAttribute("isTecnico", UsuarioAtual.isTecnico());
        return "notificacoes/list";
    }

    /** Abre a notificação: marca como lida e leva à ordem relacionada. */
    @GetMapping("/{id}")
    public String abrir(@PathVariable Long id) {
        return "redirect:" + service.abrir(id);
    }

    @PostMapping("/marcar-todas")
    public String marcarTodas() {
        service.marcarTodasComoLidas();
        return "redirect:/notificacoes";
    }
}
