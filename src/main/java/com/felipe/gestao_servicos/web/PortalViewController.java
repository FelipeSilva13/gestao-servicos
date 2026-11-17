package com.felipe.gestao_servicos.web;

import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.enums.StatusOS;
import com.felipe.gestao_servicos.service.AnexoService;
import com.felipe.gestao_servicos.service.EventoOrdemService;
import com.felipe.gestao_servicos.service.PortalClienteService;
import com.felipe.gestao_servicos.service.RegraNegocioException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Portal do cliente: acompanhamento, solicitações, orçamento, agenda, arquivos e avaliação. */
@Controller
@RequestMapping("/portal")
public class PortalViewController {

    static final String FORMATO_DATA_HORA = "yyyy-MM-dd'T'HH:mm";

    private final PortalClienteService portal;
    private final AnexoService anexoService;
    private final EventoOrdemService eventos;

    public PortalViewController(PortalClienteService portal, AnexoService anexoService, EventoOrdemService eventos) {
        this.portal = portal;
        this.anexoService = anexoService;
        this.eventos = eventos;
    }

    /** Painel + histórico de serviços, com filtro por situação. */
    @GetMapping
    public String painel(@RequestParam(defaultValue = "andamento") String filtro, Model model) {
        List<OrdemServico> todas = portal.minhasOrdens();
        List<OrdemServico> filtradas = switch (filtro) {
            case "concluidas" -> todas.stream().filter(o -> o.getStatus().isEncerrado()).toList();
            case "todas" -> todas;
            default -> todas.stream().filter(o -> !o.getStatus().isEncerrado()).toList();
        };
        Map<StatusOS, Long> contagem = portal.contagemPorStatus(todas);

        model.addAttribute("cliente", portal.clienteAtual());
        model.addAttribute("ordens", filtradas);
        model.addAttribute("filtro", filtro);
        model.addAttribute("totalAndamento", todas.stream().filter(o -> !o.getStatus().isEncerrado()).count());
        model.addAttribute("totalConcluidas", contagem.getOrDefault(StatusOS.FINALIZADA, 0L));
        model.addAttribute("totalOrcamentos", contagem.getOrDefault(StatusOS.AGUARDANDO_APROVACAO, 0L));
        model.addAttribute("avaliadas", portal.idsAvaliados(todas));
        model.addAttribute("totalGeral", todas.size());
        return "portal/painel";
    }

    @GetMapping("/solicitacoes/nova")
    public String novaSolicitacao(Model model) {
        model.addAttribute("servicos", portal.catalogoServicos());
        model.addAttribute("extensoesAceitas", AnexoService.EXTENSOES_ACEITAS);
        return "portal/solicitacao";
    }

    @PostMapping("/solicitacoes")
    public String solicitar(@RequestParam String descricao,
                            @RequestParam(required = false) Long servicoId,
                            @RequestParam(required = false) @DateTimeFormat(pattern = FORMATO_DATA_HORA) LocalDateTime dataPreferida,
                            @RequestParam(value = "arquivos", required = false) List<MultipartFile> arquivos,
                            RedirectAttributes flash) {
        try {
            OrdemServico os = portal.solicitar(descricao, servicoId, dataPreferida, arquivos);
            flash.addFlashAttribute("sucesso", "Solicitação enviada! Você será avisado a cada atualização.");
            return "redirect:/portal/ordens/" + os.getId();
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
            flash.addFlashAttribute("descricao", descricao);
            return "redirect:/portal/solicitacoes/nova";
        }
    }

    /** Acompanhamento detalhado de uma ordem. */
    @GetMapping("/ordens/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        OrdemServico os = portal.minhaOrdem(id);
        model.addAttribute("ordem", os);
        model.addAttribute("historico", eventos.historico(os));
        model.addAttribute("anexos", anexoService.listar(os));
        model.addAttribute("avaliacao", portal.avaliacao(os).orElse(null));
        model.addAttribute("versao", eventos.versao(os));
        model.addAttribute("extensoesAceitas", AnexoService.EXTENSOES_ACEITAS);
        return "portal/ordem";
    }

    @PostMapping("/ordens/{id}/orcamento/aprovar")
    public String aprovar(@PathVariable Long id, RedirectAttributes flash) {
        return executar(id, flash, "Orçamento aprovado. A empresa já foi avisada.", () -> portal.aprovarOrcamento(id));
    }

    @PostMapping("/ordens/{id}/orcamento/recusar")
    public String recusar(@PathVariable Long id, @RequestParam(required = false) String motivo, RedirectAttributes flash) {
        return executar(id, flash, "Orçamento recusado.", () -> portal.recusarOrcamento(id, motivo));
    }

    @PostMapping("/ordens/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes flash) {
        return executar(id, flash, "Solicitação cancelada.", () -> portal.cancelar(id));
    }

    @PostMapping("/ordens/{id}/agendamento")
    public String reagendar(@PathVariable Long id,
                            @RequestParam(required = false) @DateTimeFormat(pattern = FORMATO_DATA_HORA) LocalDateTime dataHora,
                            RedirectAttributes flash) {
        return executar(id, flash, "Data solicitada. Aguarde a confirmação da empresa.", () -> portal.reagendar(id, dataHora));
    }

    @PostMapping("/ordens/{id}/anexos")
    public String anexar(@PathVariable Long id,
                         @RequestParam(value = "arquivos", required = false) List<MultipartFile> arquivos,
                         RedirectAttributes flash) {
        return executar(id, flash, "Arquivos enviados.", () -> portal.anexar(id, arquivos));
    }

    @PostMapping("/ordens/{id}/avaliacao")
    public String avaliar(@PathVariable Long id,
                          @RequestParam(required = false) Integer notaServico,
                          @RequestParam(required = false) Integer notaTecnico,
                          @RequestParam(required = false) String comentario,
                          RedirectAttributes flash) {
        return executar(id, flash, "Obrigado pela avaliação!", () -> portal.avaliar(id, notaServico, notaTecnico, comentario));
    }

    private String executar(Long id, RedirectAttributes flash, String mensagemSucesso, Supplier<?> acao) {
        try {
            acao.get();
            flash.addFlashAttribute("sucesso", mensagemSucesso);
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/portal/ordens/" + id;
    }
}
