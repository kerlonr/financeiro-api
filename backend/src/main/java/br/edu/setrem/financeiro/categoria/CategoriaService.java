package br.edu.setrem.financeiro.categoria;

import br.edu.setrem.financeiro.common.RegraNegocioException;
import br.edu.setrem.financeiro.common.UsuarioLogado;
import br.edu.setrem.financeiro.transacao.Transacao;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class CategoriaService {

    @Inject
    UsuarioLogado usuarioLogado;

    public List<CategoriaResponse> listar() {
        return Categoria.<Categoria>find("usuarioId", Sort.by("nome"), usuarioLogado.id())
                .stream()
                .map(CategoriaResponse::de)
                .toList();
    }

    /** Busca uma categoria do usuário logado. Categoria de outro usuário é tratada como inexistente (404). */
    public Categoria buscar(Long id) {
        return Categoria.<Categoria>find("id = ?1 and usuarioId = ?2", id, usuarioLogado.id())
                .firstResultOptional()
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public void criar(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        categoria.usuarioId = usuarioLogado.id();
        preencher(categoria, request);
        categoria.persist();
    }

    @Transactional
    public void atualizar(Long id, CategoriaRequest request) {
        preencher(buscar(id), request);
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscar(id);
        if (Transacao.count("categoria", categoria) > 0) {
            throw new RegraNegocioException("Esta categoria possui lançamentos. Exclua-os antes de remover a categoria.");
        }
        categoria.delete();
    }

    private void preencher(Categoria categoria, CategoriaRequest request) {
        String nome = request.nome().trim();
        long categoriasComMesmoNome = Categoria.count("usuarioId = ?1 and lower(nome) = lower(?2) and id <> ?3",
                usuarioLogado.id(), nome, categoria.id == null ? 0L : categoria.id);
        if (categoriasComMesmoNome > 0) {
            throw new RegraNegocioException("Já existe uma categoria com este nome.");
        }

        categoria.nome = nome;
    }
}
