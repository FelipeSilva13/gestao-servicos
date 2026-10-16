package com.felipe.gestao_servicos.dto.response;

public record CadastroEmpresaResponse(
        Long id,
        String nomeEmpresa,
        String emailAdministrador
) {
}