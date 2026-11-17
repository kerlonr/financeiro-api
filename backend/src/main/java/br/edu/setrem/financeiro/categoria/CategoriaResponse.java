package br.edu.setrem.financeiro.categoria;

public record CategoriaResponse(Long id, String nome) {

    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.id, categoria.nome);
    }
}
