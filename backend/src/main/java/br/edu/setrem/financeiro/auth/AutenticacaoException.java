package br.edu.setrem.financeiro.auth;

/** Falha de login ou de sessão. Vira HTTP 401. */
public class AutenticacaoException extends RuntimeException {

    public AutenticacaoException(String mensagem) {
        super(mensagem);
    }
}
