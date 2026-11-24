package com.felipe.gestao_servicos.web;

import com.felipe.gestao_servicos.domain.Tecnico;
import com.felipe.gestao_servicos.dto.request.TecnicoRequest;
import com.felipe.gestao_servicos.config.multitenancy.TenantContext;
import com.felipe.gestao_servicos.repository.AvaliacaoRepository;
import com.felipe.gestao_servicos.service.AcessoTecnicoService;
import com.felipe.gestao_servicos.service.RegraNegocioException;
import com.felipe.gestao_servicos.service.TecnicoService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tecnicos")
public class TecnicoViewController {
    private final TecnicoService service;
    private final AvaliacaoRepository avaliacaoRepository;
    private final AcessoTecnicoService acessoTecnicoService;

    public TecnicoViewController(TecnicoService service, AvaliacaoRepository avaliacaoRepository,
                                 AcessoTecnicoService acessoTecnicoService) {
        this.service = service;
        this.avaliacaoRepository = avaliacaoRepository;
        this.acessoTecnicoService = acessoTecnicoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tecnicos", service.listar());
        Map<Long, String> medias = new HashMap<>();
        for (Object[] linha : avaliacaoRepository.mediasPorTecnico(TenantContext.getCurrentTenant())) {
            medias.put((Long) linha[0], String.format(Locale.of("pt", "BR"), "%.1f ★ (%d)", (Double) linha[1], (Long) linha[2]));
        }
        model.addAttribute("mediasTecnicos", medias);
        return "tecnicos/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("tecnico", new Tecnico());
        return "tecnicos/form";
    }

    @PostMapping
    public String criar(@ModelAttribute TecnicoRequest tecnico) {
        service.salvar(tecnico);
        return "redirect:/tecnicos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("tecnico", service.buscarPorId(id));
        return "tecnicos/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute TecnicoRequest tecnico) {

        service.atualizar(id, tecnico);
        return "redirect:/tecnicos";
    }

    /** Liberação do acesso do técnico à área do técnico (somente administrador, ver SecurityConfig). */
    @GetMapping("/{id}/acesso")
    public String acesso(@PathVariable Long id, Model model) {
        model.addAttribute("tecnico", acessoTecnicoService.tecnico(id));
        model.addAttribute("acesso", acessoTecnicoService.acessoExistente(id).orElse(null));
        model.addAttribute("senhaMinima", AcessoTecnicoService.SENHA_MINIMA);
        return "tecnicos/acesso";
    }

    @PostMapping("/{id}/acesso")
    public String definirAcesso(@PathVariable Long id, @RequestParam String senha, RedirectAttributes flash) {
        try {
            acessoTecnicoService.definirSenha(id, senha);
            flash.addFlashAttribute("sucesso", "Acesso do técnico liberado. Envie a senha ao técnico por um canal seguro.");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/tecnicos/" + id + "/acesso";
    }

    @PostMapping("/{id}/acesso/revogar")
    public String revogarAcesso(@PathVariable Long id, RedirectAttributes flash) {
        acessoTecnicoService.revogar(id);
        flash.addFlashAttribute("sucesso", "Acesso do técnico revogado.");
        return "redirect:/tecnicos/" + id + "/acesso";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        service.excluir(id);
        return "redirect:/tecnicos";
    }
}