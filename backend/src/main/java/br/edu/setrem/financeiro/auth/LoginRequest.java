package br.edu.setrem.financeiro.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Informe seu e-mail.")
        @Size(max = 160, message = "Use no máximo 160 caracteres.")
        String email,

        @NotBlank(message = "Informe sua senha.")
        @Size(max = 72, message = "Use no máximo 72 caracteres.")
        String senha
) {
}
