package br.edu.setrem.financeiro.conta;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ContaRequest(
        @NotBlank(message = "Informe o nome da conta.")
        @Size(max = 60, message = "Use no máximo 60 caracteres.")
        String nome,

        @NotNull(message = "Informe o saldo inicial.")
        @Digits(integer = 12, fraction = 2, message = "Valor inválido.")
        BigDecimal saldoInicial
) {
}
