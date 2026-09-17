package com.felipe.gestao_servicos.dto.response;

import com.felipe.gestao_servicos.enums.StatusOS;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record OrdemResponse(

        Long clienteId,

        Long tecnicoId,

        Long servicoId,

        StatusOS status,

        LocalDateTime dataCriacao,

        LocalDateTime dataInicio,

        LocalDateTime dataFim,

        String observacoes,

        BigDecimal valorTotal
) {
}
