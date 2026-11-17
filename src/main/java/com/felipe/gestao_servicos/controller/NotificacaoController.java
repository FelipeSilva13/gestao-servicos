package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.service.NotificacaoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Endpoints JSON das notificações, consultados pelo sino do layout. */
@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService service;

    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    /** Consultado periodicamente para atualizar o contador do sino. */
    @GetMapping("/contagem")
    public Map<String, Long> contagem() {
        return Map.of("naoLidas", service.contarNaoLidasDoUsuarioAtual());
    }
}
