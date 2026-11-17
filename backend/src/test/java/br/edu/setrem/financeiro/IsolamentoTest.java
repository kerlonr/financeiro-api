package br.edu.setrem.financeiro;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static br.edu.setrem.financeiro.Api.como;
import static org.hamcrest.Matchers.empty;

/** Um usuário nunca pode ler nem alterar dados de outro, mesmo conhecendo os ids. */
@QuarkusTest
class IsolamentoTest {

    @Test
    void usuarioNaoAcessaContaDeOutro() {
        Map<String, String> ana = Api.novoUsuario();
        Map<String, String> bruno = Api.novoUsuario();
        long contaDaAna = Api.criarConta(ana, "Corrente", "100.00");

        como(bruno).get("/api/contas").then().statusCode(200).body("$", empty());
        como(bruno).body(Map.of("nome", "Invadida", "saldoInicial", "0"))
                .put("/api/contas/" + contaDaAna).then().statusCode(404);
        como(bruno).delete("/api/contas/" + contaDaAna).then().statusCode(404);
    }

    @Test
    void usuarioNaoLancaNemMexeEmTransacaoDeOutro() {
        Map<String, String> ana = Api.novoUsuario();
        Map<String, String> bruno = Api.novoUsuario();
        long contaDaAna = Api.criarConta(ana, "Corrente", "0");
        long categoriaDaAna = Api.criarCategoria(ana, "Mercado");
        long contaDoBruno = Api.criarConta(bruno, "Carteira", "0");
        long categoriaDoBruno = Api.criarCategoria(bruno, "Lazer");

        // Bruno tenta lançar na conta da Ana e usar a categoria dela.
        como(bruno).body(Api.transacao(contaDaAna, categoriaDoBruno, "SAIDA", "10.00"))
                .post("/api/transacoes").then().statusCode(404);
        como(bruno).body(Api.transacao(contaDoBruno, categoriaDaAna, "SAIDA", "10.00"))
                .post("/api/transacoes").then().statusCode(404);

        como(ana).body(Api.transacao(contaDaAna, categoriaDaAna, "SAIDA", "10.00"))
                .post("/api/transacoes").then().statusCode(201);
        long transacaoDaAna = como(ana).get(Api.TRANSACOES_2026).then().extract().jsonPath().getLong("[0].id");

        como(bruno).get(Api.TRANSACOES_2026).then().body("$", empty());
        como(bruno).body(Api.transacao(contaDoBruno, categoriaDoBruno, "SAIDA", "1.00"))
                .put("/api/transacoes/" + transacaoDaAna).then().statusCode(404);
        como(bruno).delete("/api/transacoes/" + transacaoDaAna).then().statusCode(404);
    }

    @Test
    void usuarioNaoTransfereParaContaDeOutro() {
        Map<String, String> ana = Api.novoUsuario();
        Map<String, String> bruno = Api.novoUsuario();
        long contaDaAna = Api.criarConta(ana, "Corrente", "0");
        long contaDoBruno = Api.criarConta(bruno, "Carteira", "500.00");

        como(bruno).body(Map.of("contaOrigemId", contaDoBruno, "contaDestinoId", contaDaAna,
                        "valor", "100.00", "data", "2026-11-10"))
                .post("/api/transacoes/transferencia").then().statusCode(404);
    }
}
