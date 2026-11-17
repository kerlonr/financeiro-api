package br.edu.setrem.financeiro.auth;

public record UsuarioResponse(String nome, String email) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.nome, usuario.email);
    }
}
