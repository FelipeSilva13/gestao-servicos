package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.dto.request.RelatorioResumo;
import com.felipe.gestao_servicos.service.RelatoriosService;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/relatorios")
public class RelatoriosController {

    private final RelatoriosService osService;

    public RelatoriosController(RelatoriosService osService) {
        this.osService = osService;
    }


    @GetMapping("/tecnico/{id}")
    public RelatorioResumo relatorioPorTecnico(@PathVariable @Positive Long id) {

        return osService.gerarRelatorioPorTecnico(id);
    }

    @GetMapping("/cliente/{id}")
    public RelatorioResumo relatorioPorCliente(@PathVariable @Positive Long id) {
        return osService.gerarRelatorioPorCliente(id);
    }
}