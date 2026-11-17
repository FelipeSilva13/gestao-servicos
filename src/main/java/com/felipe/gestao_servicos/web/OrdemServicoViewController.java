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

import com.felipe.gestao_servicos.repository.AvaliacaoRepository;
import com.felipe.gestao_servicos.service.AnexoService;
import com.felipe.gestao_servicos.service.EventoOrdemService;
import com.felipe.gestao_servicos.service.RegraNegocioException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

@Controller
@RequestMapping("/os")
public class OrdemServicoViewController {
    private final OrdemServicoService service;
    private final ClienteService clienteService;
    private final TecnicoService tecnicoService;
    private final ServicoService servicoService;

    private final EventoOrdemService eventos;
    private final AnexoService anexoService;
    private final AvaliacaoRepository avaliacaoRepository;

    public OrdemServicoViewController(OrdemServicoService service, ClienteService clienteService, TecnicoService tecnicoService,
                                      ServicoService servicoService, EventoOrdemService eventos, AnexoService anexoService,
                                      AvaliacaoRepository avaliacaoRepository) {
        this.eventos = eventos;
        this.anexoService = anexoService;
        this.avaliacaoRepository = avaliacaoRepository;
        this.service = service;
        this.clienteService = clienteService;
        this.tecnicoService = tecnicoService;
        this.servicoService = servicoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ordens", service.listarEntidadesRecentes());
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
    public String atualizarStatus(@PathVariable Long id, @RequestParam("status") StatusOS status,
                                  @RequestParam(required = false) String voltar) {
        service.atualizarStatus(id, status);
        return "detalhe".equals(voltar) ? "redirect:/os/" + id : "redirect:/os";
    }

    /** Detalhe da ordem: triagem, orçamento, agenda, anexos, avaliação e histórico. */
    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        OrdemServico os = service.buscarObrigatoria(id);
        model.addAttribute("ordem", os);
        model.addAttribute("StatusOS", StatusOS.values());
        model.addAttribute("tecnicos", tecnicoService.listar());
        model.addAttribute("servicos", servicoService.listar());
        model.addAttribute("historico", eventos.historico(os));
        model.addAttribute("anexos", anexoService.listar(os));
        model.addAttribute("avaliacao", avaliacaoRepository.findByOrdemIdAndTenantId(os.getId(), os.getTenant().getId()).orElse(null));
        model.addAttribute("extensoesAceitas", AnexoService.EXTENSOES_ACEITAS);
        return "os/detalhe";
    }

    @PostMapping("/{id}/atribuir")
    public String atribuir(@PathVariable Long id, @RequestParam(required = false) Long tecnicoId,
                           @RequestParam(required = false) Long servicoId, RedirectAttributes flash) {
        return executar(id, flash, "Atribuição atualizada.", () -> service.atribuir(id, tecnicoId, servicoId));
    }

    @PostMapping("/{id}/orcamento")
    public String enviarOrcamento(@PathVariable Long id, @RequestParam(required = false) BigDecimal valor,
                                  @RequestParam(required = false) String descricao, RedirectAttributes flash) {
        return executar(id, flash, "Orçamento enviado ao cliente.", () -> service.enviarOrcamento(id, valor, descricao));
    }

    @PostMapping("/{id}/agendamento")
    public String agendar(@PathVariable Long id,
                          @RequestParam(required = false) @DateTimeFormat(pattern = PortalViewController.FORMATO_DATA_HORA) LocalDateTime dataHora,
                          @RequestParam(defaultValue = "false") boolean confirmado, RedirectAttributes flash) {
        return executar(id, flash, "Agendamento atualizado e cliente avisado.", () -> service.agendar(id, dataHora, confirmado));
    }

    @PostMapping("/{id}/anexos")
    public String anexar(@PathVariable Long id, @RequestParam(value = "arquivos", required = false) List<MultipartFile> arquivos,
                         RedirectAttributes flash) {
        return executar(id, flash, "Arquivos anexados.", () -> {
            OrdemServico os = service.buscarObrigatoria(id);
            int quantidade = anexoService.anexar(os, arquivos);
            if (quantidade == 0) throw new RegraNegocioException("Selecione ao menos um arquivo");
            eventos.registrar(os, "Empresa anexou " + quantidade + (quantidade == 1 ? " arquivo" : " arquivos"),
                    EventoOrdemService.Destino.CLIENTE);
            return quantidade;
        });
    }

    private String executar(Long id, RedirectAttributes flash, String sucesso, Supplier<?> acao) {
        try {
            acao.get();
            flash.addFlashAttribute("sucesso", sucesso);
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/os/" + id;
    }
}