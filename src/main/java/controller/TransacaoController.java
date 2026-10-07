package controller;

import entity.Categoria;
import entity.Conta;
import entity.Transacao;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import java.util.List;

@Path("transacao")
public class TransacaoController {

    @GET
    public List<Transacao> todas() {
        return Transacao.listAll();
    }

    @POST
    @Transactional
    public String save(Transacao t) {
        if (t.conta == null || t.categoria == null) {
            return "Informe a conta e a categoria";
        }
        t.conta = Conta.findById(t.conta.id);
        t.categoria = Categoria.findById(t.categoria.id);
        if (t.conta == null || t.categoria == null) {
            return "Conta ou categoria nao encontrada";
        }
        Transacao.persist(t);
        return "OK";
    }

    @PUT
    @Path("{id}")
    @Transactional
    public String update(@PathParam("id") Long id, Transacao t) {
        Transacao atual = Transacao.findById(id);
        if (atual == null) {
            return "Transacao nao encontrada";
        }
        if (t.conta == null || t.categoria == null) {
            return "Informe a conta e a categoria";
        }
        Conta conta = Conta.findById(t.conta.id);
        Categoria categoria = Categoria.findById(t.categoria.id);
        if (conta == null || categoria == null) {
            return "Conta ou categoria nao encontrada";
        }
        atual.descricao = t.descricao;
        atual.valor = t.valor;
        atual.tipo = t.tipo;
        atual.datatransacao = t.datatransacao;
        atual.conta = conta;
        atual.categoria = categoria;
        return "OK";
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public String delete(@PathParam("id") Long id) {
        if (Transacao.deleteById(id)) {
            return "OK";
        }
        return "Transacao nao encontrada";
    }
}
