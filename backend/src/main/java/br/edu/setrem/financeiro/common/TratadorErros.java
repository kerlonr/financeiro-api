package br.edu.setrem.financeiro.common;

import br.edu.setrem.financeiro.auth.AutenticacaoException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response.Status;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.Map;
import java.util.TreeMap;

/** Converte exceções em respostas HTTP com o corpo padrão {@link ErroResponse}. */
public class TratadorErros {

    @ServerExceptionMapper
    public RestResponse<ErroResponse> regraNegocio(RegraNegocioException e) {
        return RestResponse.ResponseBuilder.<ErroResponse>create(422).entity(new ErroResponse(e.getMessage())).build();
    }

    @ServerExceptionMapper
    public RestResponse<ErroResponse> autenticacao(AutenticacaoException e) {
        return RestResponse.status(Status.UNAUTHORIZED, new ErroResponse(e.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<ErroResponse> naoEncontrado(NotFoundException e) {
        return RestResponse.status(Status.NOT_FOUND, new ErroResponse("Registro não encontrado."));
    }

    @ServerExceptionMapper
    public RestResponse<ErroResponse> validacao(ConstraintViolationException e) {
        Map<String, String> campos = new TreeMap<>();
        for (ConstraintViolation<?> violacao : e.getConstraintViolations()) {
            campos.putIfAbsent(nomeDoCampo(violacao.getPropertyPath()), violacao.getMessage());
        }
        return RestResponse.status(Status.BAD_REQUEST, new ErroResponse("Dados inválidos.", campos));
    }

    private String nomeDoCampo(Path caminho) {
        String nome = "";
        for (Path.Node no : caminho) {
            nome = no.getName();
        }
        return nome;
    }
}
