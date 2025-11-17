package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.dto.RelatorioResumo;
import com.felipe.gestao_servicos.enums.StatusOS;
import com.felipe.gestao_servicos.service.OrdemServicoService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
public class RelatoriosController {
    private final OrdemServicoService osService;

    public RelatoriosController(OrdemServicoService osService) { this.osService = osService; }

    @GetMapping("/tecnico/{id}")
    public RelatorioResumo relatorioPorTecnico(@PathVariable Long id) {
        List<OrdemServico> lista = osService.listarPorTecnico(id);
        long pendentes = lista.stream().filter(o -> o.getStatus() == StatusOS.PENDENTE).count();
        long execucao = lista.stream().filter(o -> o.getStatus() == StatusOS.EM_EXECUCAO).count();
        long finalizadas = lista.stream().filter(o -> o.getStatus() == StatusOS.FINALIZADA).count();
        BigDecimal total = lista.stream().map(o -> o.getValorTotal() == null ? BigDecimal.ZERO : o.getValorTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RelatorioResumo(pendentes, execucao, finalizadas, total);
    }

    @GetMapping("/cliente/{id}")
    public RelatorioResumo relatorioPorCliente(@PathVariable Long id) {
        List<OrdemServico> lista = osService.listarPorCliente(id);
        long pendentes = lista.stream().filter(o -> o.getStatus() == StatusOS.PENDENTE).count();
        long execucao = lista.stream().filter(o -> o.getStatus() == StatusOS.EM_EXECUCAO).count();
        long finalizadas = lista.stream().filter(o -> o.getStatus() == StatusOS.FINALIZADA).count();
        BigDecimal total = lista.stream().map(o -> o.getValorTotal() == null ? BigDecimal.ZERO : o.getValorTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RelatorioResumo(pendentes, execucao, finalizadas, total);
    }
}