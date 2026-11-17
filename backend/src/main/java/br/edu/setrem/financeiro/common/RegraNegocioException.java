package br.edu.setrem.financeiro.common;

/** Violação de uma regra do domínio. Vira HTTP 422 com a mensagem para o usuário. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
