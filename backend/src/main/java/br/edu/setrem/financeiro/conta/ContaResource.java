package br.edu.setrem.financeiro.conta;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;

@Path("contas")
public class ContaResource {

    @Inject
    ContaService contaService;

    @GET
    public List<ContaResponse> listar() {
        return contaService.listarComSaldo();
    }

    @POST
    @ResponseStatus(201)
    public void criar(@Valid ContaRequest request) {
        contaService.criar(request);
    }

    @PUT
    @Path("{id}")
    public void atualizar(@RestPath Long id, @Valid ContaRequest request) {
        contaService.atualizar(id, request);
    }

    @DELETE
    @Path("{id}")
    public void excluir(@RestPath Long id) {
        contaService.excluir(id);
    }
}
