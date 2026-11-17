package br.edu.setrem.financeiro.painel;

import br.edu.setrem.financeiro.conta.ContaResponse;
import br.edu.setrem.financeiro.transacao.TransacaoResponse;

import java.math.BigDecimal;
import java.util.List;

public record PainelResponse(
        BigDecimal saldoTotal,
        List<ContaResponse> contas,
        BigDecimal entradasDoMes,
        BigDecimal saidasDoMes,
        List<TransacaoResponse> ultimasTransacoes
) {
}
