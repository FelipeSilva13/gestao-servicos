package com.felipe.gestao_servicos.dto.response;

public record TecnicoResponse(
        Long id,

        String nome,

        String email,

        String telefone,

        String especialidade,

        Boolean disponivel
) {
}
