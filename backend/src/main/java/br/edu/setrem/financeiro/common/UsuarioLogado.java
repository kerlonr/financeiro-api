package br.edu.setrem.financeiro.common;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Identifica o usuário da requisição atual a partir do JWT (claim "sub").
 * Todo acesso a dados passa por aqui para garantir que cada usuário só veja o que é seu.
 */
@RequestScoped
public class UsuarioLogado {

    @Inject
    JsonWebToken jwt;

    public Long id() {
        return Long.valueOf(jwt.getSubject());
    }
}
