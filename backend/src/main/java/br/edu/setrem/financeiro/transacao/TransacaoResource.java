package br.edu.setrem.financeiro.transacao;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import java.time.LocalDate;
import java.util.List;

@Path("transacoes")
public class TransacaoResource {

    @Inject
    TransacaoService transacaoService;

    @GET
    public List<TransacaoResponse> listar(@RestQuery Long contaId, @RestQuery Long categoriaId,
                                          @RestQuery @NotNull(message = "Informe a data inicial.") LocalDate inicio,
                                          @RestQuery @NotNull(message = "Informe a data final.") LocalDate fim) {
        return transacaoService.listar(contaId, categoriaId, inicio, fim);
    }

    @POST
    @ResponseStatus(201)
    public void criar(@Valid TransacaoRequest request) {
        transacaoService.criar(request);
    }

    @PUT
    @Path("{id}")
    public void atualizar(@RestPath Long id, @Valid TransacaoRequest request) {
        transacaoService.atualizar(id, request);
    }

    @DELETE
    @Path("{id}")
    public void excluir(@RestPath Long id) {
        transacaoService.excluir(id);
    }

    @POST
    @Path("transferencia")
    @ResponseStatus(201)
    public void transferir(@Valid TransferenciaRequest request) {
        transacaoService.transferir(request);
    }
}
