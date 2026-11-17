package br.edu.setrem.financeiro;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static br.edu.setrem.financeiro.Api.como;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class AuthTest {

    @Test
    void rotasDaApiExigemLogin() {
        given().get("/api/contas").then().statusCode(401);
        given().get("/api/painel").then().statusCode(401);
        given().get("/api/auth/me").then().statusCode(401);
    }

    @Test
    void tokenFalsificadoERecusado() {
        given().cookie("access_token", "eyJhbGciOiJub25lIn0.eyJzdWIiOiIxIn0.").get("/api/contas")
                .then().statusCode(401);
    }

    @Test
    void cadastroDevolveCookiesHttpOnlyESemTokenNoCorpo() {
        given().contentType(ContentType.JSON)
                .body(Map.of("nome", "Ana", "email", Api.emailUnico(), "senha", Api.SENHA))
                .post("/api/auth/cadastro")
                .then().statusCode(201)
                .body("nome", equalTo("Ana"))
                .body(not(containsString("eyJ")))
                .header("Set-Cookie", containsString("HttpOnly"))
                .header("Set-Cookie", containsString("SameSite=Strict"));
    }

    @Test
    void loginComSenhaErradaEEmailInexistenteTemMesmaMensagem() {
        String email = Api.emailUnico();
        cadastrar(email);

        String senhaErrada = login(email, "senha-errada-999").then().statusCode(401)
                .extract().path("mensagem");
        String emailInexistente = login(Api.emailUnico(), "qualquer-senha").then().statusCode(401)
                .extract().path("mensagem");

        assertNotNull(senhaErrada);
        assertEquals(senhaErrada, emailInexistente);
    }

    @Test
    void contaEBloqueadaAposCincoTentativasErradas() {
        String email = Api.emailUnico();
        cadastrar(email);

        for (int i = 0; i < 5; i++) {
            login(email, "senha-errada-999").then().statusCode(401);
        }

        login(email, Api.SENHA).then().statusCode(401)
                .body("mensagem", containsString("Muitas tentativas"));
    }

    @Test
    void refreshTokenERotacionadoEReusoDerrubaASessao() {
        Map<String, String> cookies = Api.novoUsuario();
        String refreshOriginal = cookies.get("refresh_token");

        Response renovacao = given().cookie("refresh_token", refreshOriginal).post("/api/auth/refresh");
        renovacao.then().statusCode(204);
        String refreshNovo = renovacao.getCookie("refresh_token");
        assertNotEquals(refreshOriginal, refreshNovo);

        // Reapresentar o token antigo indica roubo: a família inteira é revogada.
        given().cookie("refresh_token", refreshOriginal).post("/api/auth/refresh").then().statusCode(401);
        given().cookie("refresh_token", refreshNovo).post("/api/auth/refresh").then().statusCode(401);
    }

    @Test
    void logoutRevogaORefreshToken() {
        Map<String, String> cookies = Api.novoUsuario();

        como(cookies).post("/api/auth/logout").then().statusCode(204);

        given().cookie("refresh_token", cookies.get("refresh_token")).post("/api/auth/refresh")
                .then().statusCode(401);
    }

    @Test
    void cadastroValidaCampos() {
        given().contentType(ContentType.JSON)
                .body(Map.of("nome", "", "email", "invalido", "senha", "123"))
                .post("/api/auth/cadastro")
                .then().statusCode(400)
                .body("campos.nome", equalTo("Informe seu nome."))
                .body("campos.email", equalTo("E-mail inválido."))
                .body("campos.senha", equalTo("A senha deve ter entre 8 e 72 caracteres."));
    }

    private static void cadastrar(String email) {
        given().contentType(ContentType.JSON)
                .body(Map.of("nome", "Teste", "email", email, "senha", Api.SENHA))
                .post("/api/auth/cadastro").then().statusCode(201);
    }

    private static Response login(String email, String senha) {
        return given().contentType(ContentType.JSON)
                .body(Map.of("email", email, "senha", senha))
                .post("/api/auth/login");
    }
}
