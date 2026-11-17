package br.edu.setrem.financeiro.transacao;

import br.edu.setrem.financeiro.categoria.Categoria;
import br.edu.setrem.financeiro.common.EntidadeBase;
import br.edu.setrem.financeiro.conta.Conta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Transacao extends EntidadeBase {

    @Column(name = "usuario_id")
    public Long usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id")
    public Conta conta;

    /** Nula apenas nas transações geradas por uma transferência. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    public Categoria categoria;

    @Enumerated(EnumType.STRING)
    public TipoTransacao tipo;

    public BigDecimal valor;
    public String descricao;
    public LocalDate data;

    /** Liga a saída da conta de origem à entrada na conta de destino. */
    @Column(name = "transferencia_id")
    public UUID transferenciaId;
}
