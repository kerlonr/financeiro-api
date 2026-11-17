package br.edu.setrem.financeiro.common;

import java.util.Map;

/** Formato único de erro devolvido pela API. "campos" só aparece em erros de validação. */
public record ErroResponse(String mensagem, Map<String, String> campos) {

    public ErroResponse(String mensagem) {
        this(mensagem, null);
    }
}
