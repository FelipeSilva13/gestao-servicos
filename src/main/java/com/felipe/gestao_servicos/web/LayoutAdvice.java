package com.felipe.gestao_servicos.web;

import com.felipe.gestao_servicos.service.NotificacaoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Dados comuns ao layout das telas: contador de notificações não lidas. */
@ControllerAdvice(basePackages = "com.felipe.gestao_servicos.web")
public class LayoutAdvice {

    private final NotificacaoService notificacaoService;

    public LayoutAdvice(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @ModelAttribute("notificacoesNaoLidas")
    public long notificacoesNaoLidas(HttpServletRequest request) {
        // Endpoints JSON ficam em controller/ e não passam por aqui; POSTs só redirecionam.
        if (!"GET".equals(request.getMethod())) {
            return 0;
        }
        return notificacaoService.contarNaoLidasDoUsuarioAtual();
    }
}
