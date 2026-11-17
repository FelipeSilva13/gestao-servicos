package com.felipe.gestao_servicos.service;

/** Operação inválida no estado atual da ordem; a mensagem é exibida ao usuário. */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
