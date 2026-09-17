package com.felipe.gestao_servicos.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados aceitos no cadastro público de uma nova empresa. */
public record CadastroEmpresaRequest(
        @NotBlank(message = "O nome da empresa é obrigatório")
        @Size(max = 255, message = "O nome da empresa deve ter no máximo 255 caracteres")
        String nomeEmpresa,
        @NotBlank(message = "O e-mail do administrador é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres")
        String email,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres")
        String senha
) {
}
