package br.edu.setrem.financeiro.transacao;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoRequest(
        @NotNull(message = "Escolha a conta.")
        Long contaId,

        @NotNull(message = "Escolha a categoria.")
        Long categoriaId,

        @NotNull(message = "Informe se é entrada ou saída.")
        TipoTransacao tipo,

        @NotNull(message = "Informe o valor.")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
        @Digits(integer = 12, fraction = 2, message = "Valor inválido.")
        BigDecimal valor,

        @NotBlank(message = "Informe uma descrição.")
        @Size(max = 120, message = "Use no máximo 120 caracteres.")
        String descricao,

        @NotNull(message = "Informe a data.")
        LocalDate data
) {
}
