package br.edu.setrem.financeiro;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static br.edu.setrem.financeiro.Api.como;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class TransacaoTest {

    @Test
    void saldoDaContaSomaEntradasESubtraiSaidas() {
        Map<String, String> usuario = Api.novoUsuario();
        long conta = Api.criarConta(usuario, "Corrente", "100.00");
        long categoria = Api.criarCategoria(usuario, "Geral");

        como(usuario).body(Api.transacao(conta, categoria, "ENTRADA", "50.25")).post("/api/transacoes");
        como(usuario).body(Api.transacao(conta, categoria, "SAIDA", "30.10")).post("/api/transacoes");

        como(usuario).get("/api/contas").then().body("[0].saldo", equalTo(120.15f));
    }

    @Test
    void transferenciaMoveSaldoEExcluirUmLadoExcluiOsDois() {
        Map<String, String> usuario = Api.novoUsuario();
        long origem = Api.criarConta(usuario, "Corrente", "1000.00");
        long destino = Api.criarConta(usuario, "Poupança", "0");

        como(usuario).body(Map.of("contaOrigemId", origem, "contaDestinoId", destino,
                        "valor", "250.00", "data", "2026-11-10"))
                .post("/api/transacoes/transferencia").then().statusCode(201);

        como(usuario).get("/api/contas").then()
                .body("find { it.nome == 'Corrente' }.saldo", equalTo(750.0f))
                .body("find { it.nome == 'Poupança' }.saldo", equalTo(250.0f));

        long umLado = como(usuario).get(Api.TRANSACOES_2026 + "&contaId=" + destino)
                .then().body("$", hasSize(1))
                .body("[0].descricao", equalTo("Transferência de Corrente"))
                .extract().jsonPath().getLong("[0].id");

        como(usuario).delete("/api/transacoes/" + umLado).then().statusCode(204);
        como(usuario).get(Api.TRANSACOES_2026).then().body("$", hasSize(0));
    }

    @Test
    void transferenciaNaoPodeSerEditadaNemParaAMesmaConta() {
        Map<String, String> usuario = Api.novoUsuario();
        long origem = Api.criarConta(usuario, "Corrente", "0");
        long destino = Api.criarConta(usuario, "Poupança", "0");
        long categoria = Api.criarCategoria(usuario, "Geral");

        como(usuario).body(Map.of("contaOrigemId", origem, "contaDestinoId", origem,
                        "valor", "10.00", "data", "2026-11-10"))
                .post("/api/transacoes/transferencia").then().statusCode(422);

        como(usuario).body(Map.of("contaOrigemId", origem, "contaDestinoId", destino,
                        "valor", "10.00", "data", "2026-11-10"))
                .post("/api/transacoes/transferencia").then().statusCode(201);
        long lado = como(usuario).get(Api.TRANSACOES_2026).then().extract().jsonPath().getLong("[0].id");

        como(usuario).body(Api.transacao(origem, categoria, "SAIDA", "99.00"))
                .put("/api/transacoes/" + lado).then().statusCode(422);
    }

    @Test
    void filtraPorContaECategoria() {
        Map<String, String> usuario = Api.novoUsuario();
        long corrente = Api.criarConta(usuario, "Corrente", "0");
        long carteira = Api.criarConta(usuario, "Carteira", "0");
        long mercado = Api.criarCategoria(usuario, "Mercado");
        long lazer = Api.criarCategoria(usuario, "Lazer");

        como(usuario).body(Api.transacao(corrente, mercado, "SAIDA", "10.00")).post("/api/transacoes");
        como(usuario).body(Api.transacao(corrente, lazer, "SAIDA", "20.00")).post("/api/transacoes");
        como(usuario).body(Api.transacao(carteira, mercado, "SAIDA", "30.00")).post("/api/transacoes");

        como(usuario).get(Api.TRANSACOES_2026 + "&contaId=" + corrente).then().body("$", hasSize(2));
        como(usuario).get(Api.TRANSACOES_2026 + "&categoriaId=" + mercado).then().body("$", hasSize(2));
        como(usuario).get(Api.TRANSACOES_2026 + "&contaId=" + corrente + "&categoriaId=" + mercado)
                .then().body("$", hasSize(1)).body("[0].valor", equalTo(10.0f));
    }

    @Test
    void naoExcluiContaOuCategoriaComLancamentos() {
        Map<String, String> usuario = Api.novoUsuario();
        long conta = Api.criarConta(usuario, "Corrente", "0");
        long categoria = Api.criarCategoria(usuario, "Geral");
        como(usuario).body(Api.transacao(conta, categoria, "ENTRADA", "5.00")).post("/api/transacoes");

        como(usuario).delete("/api/contas/" + conta).then().statusCode(422);
        como(usuario).delete("/api/categorias/" + categoria).then().statusCode(422);
    }

    @Test
    void valoresInvalidosSaoRecusados() {
        Map<String, String> usuario = Api.novoUsuario();
        long conta = Api.criarConta(usuario, "Corrente", "0");
        long categoria = Api.criarCategoria(usuario, "Geral");

        como(usuario).body(Api.transacao(conta, categoria, "SAIDA", "-5.00"))
                .post("/api/transacoes").then().statusCode(400);
        como(usuario).body(Api.transacao(conta, categoria, "SAIDA", "1.999"))
                .post("/api/transacoes").then().statusCode(400);
    }

    @Test
    void listagemExigePeriodoDeNoMaximoUmAno() {
        Map<String, String> usuario = Api.novoUsuario();

        como(usuario).get("/api/transacoes").then().statusCode(400)
                .body("campos.inicio", equalTo("Informe a data inicial."));
        como(usuario).get("/api/transacoes?inicio=2024-01-01&fim=2026-01-01").then().statusCode(422);
        como(usuario).get("/api/transacoes?inicio=2026-02-01&fim=2026-01-01").then().statusCode(422);
    }

    @Test
    void naoPermiteContaComNomeRepetido() {
        Map<String, String> usuario = Api.novoUsuario();
        Api.criarConta(usuario, "Corrente", "0");

        como(usuario).body(Map.of("nome", "corrente", "saldoInicial", "0"))
                .post("/api/contas").then().statusCode(422);
    }

    @Test
    void painelMostraSaldoGeralETotaisDoMesSemContarTransferencias() {
        Map<String, String> usuario = Api.novoUsuario();
        long corrente = Api.criarConta(usuario, "Corrente", "100.00");
        long poupanca = Api.criarConta(usuario, "Poupança", "50.00");
        long categoria = Api.criarCategoria(usuario, "Geral");
        String hoje = LocalDate.now().toString();

        Map<String, Object> entrada = new HashMap<>(Api.transacao(corrente, categoria, "ENTRADA", "40.00"));
        entrada.put("data", hoje);
        Map<String, Object> saida = new HashMap<>(Api.transacao(poupanca, categoria, "SAIDA", "15.00"));
        saida.put("data", hoje);
        como(usuario).body(entrada).post("/api/transacoes");
        como(usuario).body(saida).post("/api/transacoes");
        como(usuario).body(Map.of("contaOrigemId", corrente, "contaDestinoId", poupanca,
                "valor", "20.00", "data", hoje)).post("/api/transacoes/transferencia");

        como(usuario).get("/api/painel").then().statusCode(200)
                .body("saldoTotal", equalTo(175.0f))
                .body("entradasDoMes", equalTo(40.0f))
                .body("saidasDoMes", equalTo(15.0f))
                .body("contas", hasSize(2))
                .body("ultimasTransacoes", hasSize(4));
    }
}
