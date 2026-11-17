package br.edu.setrem.financeiro.conta;

import br.edu.setrem.financeiro.common.RegraNegocioException;
import br.edu.setrem.financeiro.common.UsuarioLogado;
import br.edu.setrem.financeiro.transacao.TipoTransacao;
import br.edu.setrem.financeiro.transacao.Transacao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class ContaService {

    // O saldo é sempre calculado a partir das transações, nunca guardado:
    // assim não existe risco de o valor exibido divergir dos lançamentos.
    private static final String CONSULTA_SALDOS = """
            select new br.edu.setrem.financeiro.conta.ContaResponse(
                c.id, c.nome, c.saldoInicial,
                c.saldoInicial + coalesce(sum(case when t.tipo = :entrada then t.valor else -t.valor end), 0))
            from Conta c left join Transacao t on t.conta = c
            where c.usuarioId = :usuarioId
            group by c.id, c.nome, c.saldoInicial
            order by c.nome
            """;

    @Inject
    UsuarioLogado usuarioLogado;

    public List<ContaResponse> listarComSaldo() {
        return Conta.getEntityManager()
                .createQuery(CONSULTA_SALDOS, ContaResponse.class)
                .setParameter("entrada", TipoTransacao.ENTRADA)
                .setParameter("usuarioId", usuarioLogado.id())
                .getResultList();
    }

    /** Busca uma conta do usuário logado. Conta de outro usuário é tratada como inexistente (404). */
    public Conta buscar(Long id) {
        return Conta.<Conta>find("id = ?1 and usuarioId = ?2", id, usuarioLogado.id())
                .firstResultOptional()
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public void criar(ContaRequest request) {
        Conta conta = new Conta();
        conta.usuarioId = usuarioLogado.id();
        preencher(conta, request);
        conta.persist();
    }

    @Transactional
    public void atualizar(Long id, ContaRequest request) {
        preencher(buscar(id), request);
    }

    @Transactional
    public void excluir(Long id) {
        Conta conta = buscar(id);
        if (Transacao.count("conta", conta) > 0) {
            throw new RegraNegocioException("Esta conta possui lançamentos. Exclua-os antes de remover a conta.");
        }
        conta.delete();
    }

    private void preencher(Conta conta, ContaRequest request) {
        String nome = request.nome().trim();
        long contasComMesmoNome = Conta.count("usuarioId = ?1 and lower(nome) = lower(?2) and id <> ?3",
                usuarioLogado.id(), nome, conta.id == null ? 0L : conta.id);
        if (contasComMesmoNome > 0) {
            throw new RegraNegocioException("Já existe uma conta com este nome.");
        }

        conta.nome = nome;
        conta.saldoInicial = request.saldoInicial();
    }
}
