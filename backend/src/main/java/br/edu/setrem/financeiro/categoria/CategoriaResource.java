package br.edu.setrem.financeiro.categoria;

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

@Path("categorias")
public class CategoriaResource {

    @Inject
    CategoriaService categoriaService;

    @GET
    public List<CategoriaResponse> listar() {
        return categoriaService.listar();
    }

    @POST
    @ResponseStatus(201)
    public void criar(@Valid CategoriaRequest request) {
        categoriaService.criar(request);
    }

    @PUT
    @Path("{id}")
    public void atualizar(@RestPath Long id, @Valid CategoriaRequest request) {
        categoriaService.atualizar(id, request);
    }

    @DELETE
    @Path("{id}")
    public void excluir(@RestPath Long id) {
        categoriaService.excluir(id);
    }
}
