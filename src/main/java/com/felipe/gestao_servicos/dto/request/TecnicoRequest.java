package com.felipe.gestao_servicos.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record TecnicoRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracter")
        String nome,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 160, message = "O email deve ter no máximo 160 caracteres")
        String email,

        @Pattern(
                regexp = "\\d{10,11}",
                message = "Telefone deve conter apenas números e ter 10 ou 11 dígitos")
        String telefone,

        @Size(max = 100, message = "A especialidade deve ter no máximo 100 caracteres")
        String especialidade,

        Boolean disponivel
) {
}
