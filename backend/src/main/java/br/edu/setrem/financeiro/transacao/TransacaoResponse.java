package br.edu.setrem.financeiro.transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransacaoResponse(
        Long id,
        TipoTransacao tipo,
        BigDecimal valor,
        String descricao,
        LocalDate data,
        Long contaId,
        String contaNome,
        Long categoriaId,
        String categoriaNome,
        UUID transferenciaId
) {

    public static TransacaoResponse de(Transacao transacao) {
        boolean temCategoria = transacao.categoria != null;
        return new TransacaoResponse(
                transacao.id,
                transacao.tipo,
                transacao.valor,
                transacao.descricao,
                transacao.data,
                transacao.conta.id,
                transacao.conta.nome,
                temCategoria ? transacao.categoria.id : null,
                temCategoria ? transacao.categoria.nome : null,
                transacao.transferenciaId);
    }
}
