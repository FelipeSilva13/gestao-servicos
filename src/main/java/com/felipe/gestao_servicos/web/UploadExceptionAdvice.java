package com.felipe.gestao_servicos.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.net.URI;

/**
 * O limite de upload estoura antes de o controller ser escolhido, por isso este
 * advice não tem filtro de pacote. Volta para a página de origem com a mensagem.
 */
@ControllerAdvice
public class UploadExceptionAdvice {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String arquivoGrandeDemais(HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request)
                .put("erro", "Arquivo grande demais. O limite é de 25 MB por arquivo e 100 MB por envio.");
        return "redirect:" + caminhoDeOrigem(request.getHeader("Referer"));
    }

    /** Usa apenas o caminho do Referer, nunca o host, para não redirecionar para fora do sistema. */
    private static String caminhoDeOrigem(String referer) {
        if (referer == null) return "/";
        try {
            String caminho = URI.create(referer).getPath();
            return caminho == null || caminho.isBlank() || !caminho.startsWith("/") || caminho.startsWith("//") ? "/" : caminho;
        } catch (IllegalArgumentException e) {
            return "/";
        }
    }
}
