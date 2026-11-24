package com.felipe.gestao_servicos.enums;

/** Resposta do técnico à ordem de serviço para a qual a empresa o designou. */
public enum RespostaTecnico {
    PENDENTE("Aguardando resposta do técnico", "warning"),
    ACEITA("Aceito pelo técnico", "success"),
    RECUSADA("Recusado pelo técnico", "danger");

    private final String label;
    private final String tom;

    RespostaTecnico(String label, String tom) {
        this.label = label;
        this.tom = tom;
    }

    public String getLabel() {
        return label;
    }

    /** Classe visual do badge: info, warning, success ou danger. */
    public String getTom() {
        return tom;
    }
}
