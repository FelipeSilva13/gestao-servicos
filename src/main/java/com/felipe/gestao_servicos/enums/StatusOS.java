package com.felipe.gestao_servicos.enums;

import java.util.EnumSet;
import java.util.Set;

public enum StatusOS {
    SOLICITADA("Solicitada", "info"),
    AGUARDANDO_APROVACAO("Aguardando aprovação", "warning"),
    PENDENTE("Pendente", "info"),
    EM_EXECUCAO("Em execução", "warning"),
    FINALIZADA("Finalizada", "success"),
    RECUSADA("Orçamento recusado", "danger"),
    CANCELADA("Cancelada", "danger");

    /** Status que encerram a ordem (não contam como abertas). */
    public static final Set<StatusOS> ENCERRADOS = EnumSet.of(FINALIZADA, RECUSADA, CANCELADA);

    private final String label;
    private final String tom;

    StatusOS(String label, String tom) {
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

    public boolean isEncerrado() {
        return ENCERRADOS.contains(this);
    }

    /**
     * Etapa no acompanhamento do cliente: 1 recebida, 2 orçamento, 3 aprovada,
     * 4 em execução, 5 concluída; 0 quando interrompida (recusada/cancelada).
     */
    public int getEtapa() {
        return switch (this) {
            case SOLICITADA -> 1;
            case AGUARDANDO_APROVACAO -> 2;
            case PENDENTE -> 3;
            case EM_EXECUCAO -> 4;
            case FINALIZADA -> 5;
            case RECUSADA, CANCELADA -> 0;
        };
    }
}
