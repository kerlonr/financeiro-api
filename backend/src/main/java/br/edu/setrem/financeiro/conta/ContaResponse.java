package br.edu.setrem.financeiro.conta;

import java.math.BigDecimal;

/** Conta com o saldo atual: saldo inicial + entradas - saídas. */
public record ContaResponse(Long id, String nome, BigDecimal saldoInicial, BigDecimal saldo) {
}
