package com.felipe.gestao_servicos.service;


import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.felipe.gestao_servicos.dto.request.RelatorioResumo;
import com.felipe.gestao_servicos.dto.response.OrdemResponse;
import com.felipe.gestao_servicos.enums.StatusOS;

@Service
public class RelatoriosService {
    private final OrdemServicoService osService;

    public RelatoriosService(OrdemServicoService osService) {
        this.osService = osService;
    }
    public RelatorioResumo gerarRelatorioPorTecnico(Long tecnicoId) {
        List<OrdemResponse> lista = osService.listarPorTecnico(tecnicoId);
        long pendentes = lista
                .stream()
                .filter(o -> o.status() == StatusOS.PENDENTE)
                .count();

        long execucao = lista
                .stream()
                .filter(o -> o.status() == StatusOS.EM_EXECUCAO)
                .count();

        long finalizadas = lista
                .stream()
                .filter(o -> o.status() == StatusOS.FINALIZADA)
                .count();

        BigDecimal total = lista
                .stream()
                .map(o -> o.valorTotal() == null
                        ? BigDecimal.ZERO
                        : o.valorTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

         return new

        RelatorioResumo(
                pendentes,
                execucao,
                finalizadas,
                total);
    }

    public RelatorioResumo gerarRelatorioPorCliente(Long clienteId) {
        List<OrdemResponse> listaPorClient = osService.listarPorCliente(clienteId);
        long pendentes = listaPorClient
                .stream()
                .filter(o -> o.status() == StatusOS.PENDENTE)
                .count();

        long execucao = listaPorClient
                .stream()
                .filter(o -> o.status() == StatusOS.EM_EXECUCAO)
                .count();

        long finalizadas = listaPorClient
                .stream()
                .filter(o -> o.status() == StatusOS.FINALIZADA)
                .count();

        BigDecimal total = listaPorClient
                .stream()
                .map(o -> o.valorTotal() == null
                        ? BigDecimal.ZERO
                        : o.valorTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return new RelatorioResumo(
                pendentes,
                execucao,
                finalizadas,
                total);

        }
}
