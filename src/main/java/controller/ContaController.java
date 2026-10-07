package controller;

import entity.Conta;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import java.util.List;

@Path("conta")
public class ContaController {

    @GET
    public List<Conta> todas() {
        return Conta.listAll();
    }

    @POST
    @Transactional
    public String save(Conta conta) {
        Conta.persist(conta);
        return "OK";
    }

    @PUT
    @Path("{id}")
    @Transactional
    public String update(@PathParam("id") Long id, Conta conta) {
        Conta c = Conta.findById(id);
        if (c == null) {
            return "Conta nao encontrada";
        }
        c.descricao = conta.descricao;
        return "OK";
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public String delete(@PathParam("id") Long id) {
        if (Conta.deleteById(id)) {
            return "OK";
        }
        return "Conta nao encontrada";
    }
}
