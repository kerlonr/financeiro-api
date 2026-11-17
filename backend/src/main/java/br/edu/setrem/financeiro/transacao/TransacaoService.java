package br.edu.setrem.financeiro.transacao;

import br.edu.setrem.financeiro.categoria.CategoriaService;
import br.edu.setrem.financeiro.common.RegraNegocioException;
import br.edu.setrem.financeiro.common.UsuarioLogado;
import br.edu.setrem.financeiro.conta.Conta;
import br.edu.setrem.financeiro.conta.ContaService;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransacaoService {

    // "join fetch" traz conta e categoria na mesma consulta (evita uma consulta extra por linha).
    private static final String CONSULTA_DO_USUARIO =
            "from Transacao t join fetch t.conta left join fetch t.categoria where t.usuarioId = :usuarioId";
    private static final String ORDEM_MAIS_RECENTES = " order by t.data desc, t.id desc";
    private static final int MAXIMO_DIAS_POR_CONSULTA = 366;

    @Inject
    UsuarioLogado usuarioLogado;

    @Inject
    ContaService contaService;

    @Inject
    CategoriaService categoriaService;

    /** Lista as transações do usuário no período. Conta e categoria são filtros opcionais. */
    public List<TransacaoResponse> listar(Long contaId, Long categoriaId, LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);
        StringBuilder consulta = new StringBuilder(CONSULTA_DO_USUARIO + " and t.data between :inicio and :fim");
        Parameters parametros = Parameters.with("usuarioId", usuarioLogado.id())
                .and("inicio", inicio)
                .and("fim", fim);

        if (contaId != null) {
            consulta.append(" and t.conta.id = :contaId");
            parametros.and("contaId", contaId);
        }
        if (categoriaId != null) {
            consulta.append(" and t.categoria.id = :categoriaId");
            parametros.and("categoriaId", categoriaId);
        }

        return Transacao.<Transacao>find(consulta + ORDEM_MAIS_RECENTES, parametros)
                .stream()
                .map(TransacaoResponse::de)
                .toList();
    }

    public List<TransacaoResponse> ultimas(int quantidade) {
        return Transacao.<Transacao>find(CONSULTA_DO_USUARIO + ORDEM_MAIS_RECENTES,
                        Parameters.with("usuarioId", usuarioLogado.id()))
                .page(0, quantidade)
                .stream()
                .map(TransacaoResponse::de)
                .toList();
    }

    /** Soma das entradas ou saídas no período, sem contar transferências entre contas próprias. */
    public BigDecimal total(TipoTransacao tipo, LocalDate inicio, LocalDate fim) {
        return Transacao.getEntityManager()
                .createQuery("""
                        select coalesce(sum(t.valor), 0) from Transacao t
                        where t.usuarioId = :usuarioId and t.tipo = :tipo and t.transferenciaId is null
                          and t.data between :inicio and :fim
                        """, BigDecimal.class)
                .setParameter("usuarioId", usuarioLogado.id())
                .setParameter("tipo", tipo)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getSingleResult();
    }

    @Transactional
    public void criar(TransacaoRequest request) {
        Transacao transacao = new Transacao();
        transacao.usuarioId = usuarioLogado.id();
        preencher(transacao, request);
        transacao.persist();
    }

    @Transactional
    public void atualizar(Long id, TransacaoRequest request) {
        Transacao transacao = buscar(id);
        if (transacao.transferenciaId != null) {
            throw new RegraNegocioException("Transferências não podem ser editadas. Exclua e lance novamente.");
        }
        preencher(transacao, request);
    }

    /** Excluir um lado de uma transferência exclui os dois, para o dinheiro não "sumir" de uma conta. */
    @Transactional
    public void excluir(Long id) {
        Transacao transacao = buscar(id);
        if (transacao.transferenciaId != null) {
            Transacao.delete("transferenciaId = ?1 and usuarioId = ?2", transacao.transferenciaId, transacao.usuarioId);
        } else {
            transacao.delete();
        }
    }

    /** Uma transferência vira duas transações: saída na origem e entrada no destino, na mesma transação do banco. */
    @Transactional
    public void transferir(TransferenciaRequest request) {
        if (request.contaOrigemId().equals(request.contaDestinoId())) {
            throw new RegraNegocioException("Escolha contas de origem e destino diferentes.");
        }

        Conta origem = contaService.buscar(request.contaOrigemId());
        Conta destino = contaService.buscar(request.contaDestinoId());
        UUID transferenciaId = UUID.randomUUID();
        boolean semDescricao = request.descricao() == null || request.descricao().isBlank();

        lancarTransferencia(origem, TipoTransacao.SAIDA, request, transferenciaId,
                semDescricao ? "Transferência para " + destino.nome : request.descricao().trim());
        lancarTransferencia(destino, TipoTransacao.ENTRADA, request, transferenciaId,
                semDescricao ? "Transferência de " + origem.nome : request.descricao().trim());
    }

    // Limita o tamanho da consulta: ninguém carrega anos de lançamentos de uma vez.
    private static void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (fim.isBefore(inicio)) {
            throw new RegraNegocioException("A data final deve ser igual ou posterior à inicial.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) > MAXIMO_DIAS_POR_CONSULTA) {
            throw new RegraNegocioException("Consulte no máximo um ano por vez.");
        }
    }

    private Transacao buscar(Long id) {
        return Transacao.<Transacao>find("id = ?1 and usuarioId = ?2", id, usuarioLogado.id())
                .firstResultOptional()
                .orElseThrow(NotFoundException::new);
    }

    private void preencher(Transacao transacao, TransacaoRequest request) {
        transacao.conta = contaService.buscar(request.contaId());
        transacao.categoria = categoriaService.buscar(request.categoriaId());
        transacao.tipo = request.tipo();
        transacao.valor = request.valor();
        transacao.descricao = request.descricao().trim();
        transacao.data = request.data();
    }

    private void lancarTransferencia(Conta conta, TipoTransacao tipo, TransferenciaRequest request,
                                     UUID transferenciaId, String descricao) {
        Transacao transacao = new Transacao();
        transacao.usuarioId = usuarioLogado.id();
        transacao.conta = conta;
        transacao.tipo = tipo;
        transacao.valor = request.valor();
        transacao.descricao = descricao;
        transacao.data = request.data();
        transacao.transferenciaId = transferenciaId;
        transacao.persist();
    }
}
