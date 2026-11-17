package br.edu.setrem.financeiro;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

/** Atalhos para os testes: cria usuários e monta requisições já autenticadas. */
public final class Api {

    public static final String SENHA = "senha-forte-123";
    /** Os testes lançam transações em 2026; a listagem exige um período. */
    public static final String TRANSACOES_2026 = "/api/transacoes?inicio=2026-01-01&fim=2026-12-31";

    private Api() {
    }

    public static String emailUnico() {
        return "teste-" + UUID.randomUUID() + "@exemplo.com";
    }

    /** Cadastra um usuário novo e devolve os cookies de sessão. */
    public static Map<String, String> novoUsuario() {
        return given().contentType(ContentType.JSON)
                .body(Map.of("nome", "Teste", "email", emailUnico(), "senha", SENHA))
                .post("/api/auth/cadastro")
                .then().statusCode(201)
                .extract().cookies();
    }

    public static RequestSpecification como(Map<String, String> cookies) {
        return given().contentType(ContentType.JSON).cookies(cookies);
    }

    public static long criarConta(Map<String, String> cookies, String nome, String saldoInicial) {
        como(cookies).body(Map.of("nome", nome, "saldoInicial", saldoInicial))
                .post("/api/contas").then().statusCode(201);
        return idPorNome(cookies, "/api/contas", nome);
    }

    public static long criarCategoria(Map<String, String> cookies, String nome) {
        como(cookies).body(Map.of("nome", nome))
                .post("/api/categorias").then().statusCode(201);
        return idPorNome(cookies, "/api/categorias", nome);
    }

    public static Map<String, Object> transacao(long contaId, long categoriaId, String tipo, String valor) {
        return Map.of("contaId", contaId, "categoriaId", categoriaId, "tipo", tipo,
                "valor", valor, "descricao", "Lançamento de teste", "data", "2026-11-10");
    }

    private static long idPorNome(Map<String, String> cookies, String caminho, String nome) {
        return como(cookies).get(caminho).then().statusCode(200)
                .extract().jsonPath().getLong("find { it.nome == '" + nome + "' }.id");
    }
}
