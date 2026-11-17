package br.edu.setrem.financeiro.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "Informe o nome da categoria.")
        @Size(max = 60, message = "Use no máximo 60 caracteres.")
        String nome
) {
}
