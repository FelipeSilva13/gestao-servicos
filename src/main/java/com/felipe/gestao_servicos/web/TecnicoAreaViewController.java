package com.felipe.gestao_servicos.web;

import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.enums.RespostaTecnico;
import com.felipe.gestao_servicos.service.AnexoService;
import com.felipe.gestao_servicos.service.PortalTecnicoService;
import com.felipe.gestao_servicos.service.RegraNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.function.Supplier;

/** Área do técnico: atendimentos designados, aceite/recusa e notificações. */
@Controller
@RequestMapping("/tecnico")
public class TecnicoAreaViewController {

    private final PortalTecnicoService area;
    private final AnexoService anexoService;

    public TecnicoAreaViewController(PortalTecnicoService area, AnexoService anexoService) {
        this.area = area;
        this.anexoService = anexoService;
    }

    @GetMapping
    public String painel(@RequestParam(defaultValue = "pendentes") String filtro, Model model) {
        List<OrdemServico> todas = area.minhasOrdens();
        List<OrdemServico> pendentes = todas.stream().filter(TecnicoAreaViewController::aguardaResposta).toList();
        List<OrdemServico> andamento = todas.stream()
                .filter(o -> o.getTecnicoResposta() == RespostaTecnico.ACEITA && !o.getStatus().isEncerrado()).toList();
        List<OrdemServico> historico = todas.stream()
                .filter(o -> o.getStatus().isEncerrado() || o.getTecnicoResposta() == RespostaTecnico.RECUSADA).toList();

        model.addAttribute("tecnico", area.tecnicoAtual());
        model.addAttribute("filtro", filtro);
        model.addAttribute("ordens", switch (filtro) {
            case "andamento" -> andamento;
            case "historico" -> historico;
            default -> pendentes;
        });
        model.addAttribute("totalPendentes", pendentes.size());
        model.addAttribute("totalAndamento", andamento.size());
        model.addAttribute("totalHistorico", historico.size());
        return "tecnico/painel";
    }

    @GetMapping("/ordens/{id}")
    public String ordem(@PathVariable Long id, Model model) {
        OrdemServico os = area.minhaOrdem(id);
        model.addAttribute("ordem", os);
        model.addAttribute("podeResponder", aguardaResposta(os));
        model.addAttribute("anexos", anexoService.listar(os));
        return "tecnico/ordem";
    }

    @PostMapping("/ordens/{id}/aceitar")
    public String aceitar(@PathVariable Long id, RedirectAttributes flash) {
        return executar(id, flash, "Atendimento aceito. A empresa foi avisada.", () -> area.aceitar(id));
    }

    @PostMapping("/ordens/{id}/recusar")
    public String recusar(@PathVariable Long id, @RequestParam(required = false) String motivo, RedirectAttributes flash) {
        return executar(id, flash, "Atendimento recusado. A empresa foi avisada.", () -> area.recusar(id, motivo));
    }

    private static boolean aguardaResposta(OrdemServico os) {
        return os.getTecnicoResposta() == RespostaTecnico.PENDENTE && !os.getStatus().isEncerrado();
    }

    private String executar(Long id, RedirectAttributes flash, String sucesso, Supplier<?> acao) {
        try {
            acao.get();
            flash.addFlashAttribute("sucesso", sucesso);
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/tecnico/ordens/" + id;
    }
}
