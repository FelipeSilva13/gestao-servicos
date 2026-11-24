package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.service.AgendaService;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Converte a agenda em JSON. A versão do cliente nunca expõe os atendimentos de outros clientes. */
final class AgendaJson {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private AgendaJson() {
    }

    static Map<String, Object> paraCliente(YearMonth mes, List<AgendaService.Dia> dias) {
        return mes(mes, dias.stream().map(d -> dia(d, false)).toList());
    }

    static Map<String, Object> paraEquipe(YearMonth mes, List<AgendaService.Dia> dias) {
        return mes(mes, dias.stream().map(d -> dia(d, true)).toList());
    }

    private static Map<String, Object> mes(YearMonth mes, List<Map<String, Object>> dias) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("mes", mes.toString());
        json.put("intervaloHoras", AgendaService.INTERVALO_MINIMO.toHours());
        json.put("dias", dias);
        return json;
    }

    private static Map<String, Object> dia(AgendaService.Dia d, boolean equipe) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("data", d.data().toString());
        json.put("situacao", d.situacao().name());
        json.put("horarios", d.horariosLivres().stream().map(HORA::format).toList());
        if (equipe) {
            json.put("aberto", d.isAberto());
            json.put("inicio", d.inicio() == null ? null : HORA.format(d.inicio()));
            json.put("fim", d.fim() == null ? null : HORA.format(d.fim()));
            json.put("ocupacoes", d.ocupacoes().stream().map(o -> {
                Map<String, Object> oc = new LinkedHashMap<>();
                oc.put("ordemId", o.ordemId());
                oc.put("numero", o.numero());
                oc.put("hora", HORA.format(o.dataHora()));
                oc.put("confirmado", o.confirmado());
                oc.put("cliente", o.cliente());
                return oc;
            }).toList());
        }
        return json;
    }
}
