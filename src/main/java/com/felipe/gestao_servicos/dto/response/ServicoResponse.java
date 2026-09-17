package com.felipe.gestao_servicos.dto.response;

import java.math.BigDecimal;

public record ServicoResponse(
        Long id,
        String descricao,
        String tipo,
        BigDecimal custo,
        Integer tempoEstimadoMinutos
) {
}
