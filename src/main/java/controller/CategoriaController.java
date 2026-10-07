package controller;

import entity.Categoria;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import java.util.List;

@Path("categoria")
public class CategoriaController {

    @GET
    public List<Categoria> todas() {
        return Categoria.listAll();
    }

    @POST
    @Transactional
    public String save(Categoria cat) {
        Categoria.persist(cat);
        return "OK";
    }

    @PUT
    @Path("{id}")
    @Transactional
    public String update(@PathParam("id") Long id, Categoria cat) {
        Categoria c = Categoria.findById(id);
        if (c == null) {
            return "Categoria nao encontrada";
        }
        c.descricao = cat.descricao;
        return "OK";
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public String delete(@PathParam("id") Long id) {
        if (Categoria.deleteById(id)) {
            return "OK";
        }
        return "Categoria nao encontrada";
    }
}
