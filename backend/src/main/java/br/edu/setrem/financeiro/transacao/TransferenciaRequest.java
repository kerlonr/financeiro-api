package br.edu.setrem.financeiro.transacao;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferenciaRequest(
        @NotNull(message = "Escolha a conta de origem.")
        Long contaOrigemId,

        @NotNull(message = "Escolha a conta de destino.")
        Long contaDestinoId,

        @NotNull(message = "Informe o valor.")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
        @Digits(integer = 12, fraction = 2, message = "Valor inválido.")
        BigDecimal valor,

        @NotNull(message = "Informe a data.")
        LocalDate data,

        /** Opcional. Sem descrição, cada lado recebe "Transferência para/de <conta>". */
        @Size(max = 120, message = "Use no máximo 120 caracteres.")
        String descricao
) {
}
