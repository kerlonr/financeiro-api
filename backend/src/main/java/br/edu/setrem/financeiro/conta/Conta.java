package br.edu.setrem.financeiro.conta;

import br.edu.setrem.financeiro.common.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class Conta extends EntidadeBase {

    @Column(name = "usuario_id")
    public Long usuarioId;

    public String nome;

    @Column(name = "saldo_inicial")
    public BigDecimal saldoInicial;
}
