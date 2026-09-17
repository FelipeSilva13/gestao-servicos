package com.felipe.gestao_servicos.dto.response;

public record ClienteResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        String endereco
) {
}
