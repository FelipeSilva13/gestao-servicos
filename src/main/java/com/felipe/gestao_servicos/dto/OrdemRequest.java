package com.felipe.gestao_servicos.dto;

import com.felipe.gestao_servicos.domain.Cliente;
import com.felipe.gestao_servicos.domain.Servico;
import com.felipe.gestao_servicos.domain.Tecnico;
import com.felipe.gestao_servicos.enums.StatusOS;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdemRequest(

        @NotNull(message = "O cliente é obrigatório")
        Long cliente_id,

        @NotNull(message = "O técnico é obrigatório")
        Long tecnico_id,

        @NotNull(message = "O serviço é obrigatório")
        Long servico_id,

        StatusOS status,

        LocalDateTime dataCriacao,

        LocalDateTime dataInicio,

        LocalDateTime dataFim,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
        String observacoes,

        BigDecimal valorTotal
){
}
