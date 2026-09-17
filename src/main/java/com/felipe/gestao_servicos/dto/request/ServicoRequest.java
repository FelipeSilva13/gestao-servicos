package com.felipe.gestao_servicos.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ServicoRequest(

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres")
        String descricao,

        @Size(max = 100, message = "O tipo deve ter no máximo 100 caracteres")
        String tipo,

        @DecimalMin(value = "0.0", inclusive = true,
                message = "O custo não pode ser negativo")
        BigDecimal custo,

        @NotNull(message = "O tempo estimado é obrigatório")
        @Positive(message = "O tempo estimado deve ser maior que zero")
        @Min(value = 1, message = "O tempo estimado deve ser maior que 0")
        @Max(value = 1440, message = "O tempo estimado não pode ultrapassar as 24 horas")
        Integer tempoEstimadoMinutos
){
}
