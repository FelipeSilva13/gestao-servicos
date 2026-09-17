package com.felipe.gestao_servicos.dto.request;

import com.felipe.gestao_servicos.enums.StatusOS;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdemRequest(

        @NotNull(message = "O cliente é obrigatório")
        @Positive(message = "O cliente deve ser válido")
        Long clienteId,

        @NotNull(message = "O técnico é obrigatório")
        @Positive(message = "O técnico deve ser válido")
        Long tecnicoId,

        @NotNull(message = "O serviço é obrigatório")
        @Positive(message = "O serviço deve ser válido")
        Long servicoId,

        @NotNull(message = "O status é obrigatório")
        StatusOS status,

        @NotNull(message = "A data de criação é obrigatória")
        LocalDateTime dataCriacao,

        LocalDateTime dataInicio,

        LocalDateTime dataFim,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
        String observacoes,

        @DecimalMin(value = "0.0", inclusive = true, message = "O valor total não pode ser negativo")
        BigDecimal valorTotal
){
}
