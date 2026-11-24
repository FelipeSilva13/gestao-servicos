package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.service.AgendaService;
import com.felipe.gestao_servicos.service.RegraNegocioException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/** Agenda da empresa (equipe): consulta do mês e abertura/fechamento de dias. */
@RestController
@RequestMapping("/api/agenda")
public class AgendaController {

    public record AbrirDiasRequest(List<LocalDate> datas, LocalTime inicio, LocalTime fim) {}

    private final AgendaService agenda;

    public AgendaController(AgendaService agenda) {
        this.agenda = agenda;
    }

    /** Situação de cada dia do mês, com os atendimentos marcados. */
    @GetMapping
    public Map<String, Object> mes(@RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes,
                                   @RequestParam(required = false) Long ordemId) {
        return AgendaJson.paraEquipe(mes, agenda.mes(mes, ordemId));
    }

    @PostMapping("/dias")
    public Map<String, Object> abrir(@RequestBody AbrirDiasRequest request) {
        int abertos = agenda.abrirDias(request.datas(), request.inicio(), request.fim()).size();
        return Map.of("abertos", abertos);
    }

    @DeleteMapping("/dias/{data}")
    public ResponseEntity<Void> fechar(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        agenda.fecharDia(data);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, String>> regra(RegraNegocioException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
    }
}
