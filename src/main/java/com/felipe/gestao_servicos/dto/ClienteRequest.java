package com.felipe.gestao_servicos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "o nome deve ter no maximo 120 caracteres")
        String nome,

        @NotBlank(message = "O email é obrigatório")
        @Size(max = 601, message = "O email deve ser no máximo 160 caracteres")
        @Email(message = "email inválido")
        String email,

        @Size(max = 11)
        String telefone,

        @Size(max = 255)
        String endereço
) {
}
