package br.edu.setrem.financeiro.painel;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("painel")
public class PainelResource {

    @Inject
    PainelService painelService;

    @GET
    public PainelResponse resumo() {
        return painelService.resumo();
    }
}
