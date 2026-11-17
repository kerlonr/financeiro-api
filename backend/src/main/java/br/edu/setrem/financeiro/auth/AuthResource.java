package br.edu.setrem.financeiro.auth;

import br.edu.setrem.financeiro.common.UsuarioLogado;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response.Status;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import java.time.Duration;

/**
 * Login, cadastro e sessão. Os tokens nunca vão no corpo da resposta:
 * ficam em cookies httpOnly, inacessíveis ao JavaScript da página.
 */
@Path("auth")
public class AuthResource {

    // O nome do cookie de acesso precisa ser o mesmo de "mp.jwt.token.cookie".
    private static final String COOKIE_ACESSO = "access_token";
    private static final String COOKIE_RENOVACAO = "refresh_token";
    private static final String PATH_ACESSO = "/api";
    private static final String PATH_RENOVACAO = "/api/auth";

    @Inject
    AuthService authService;

    @Inject
    SessaoService sessaoService;

    @Inject
    AuthConfig config;

    @Inject
    UsuarioLogado usuarioLogado;

    @POST
    @Path("cadastro")
    public RestResponse<UsuarioResponse> cadastrar(@Valid CadastroRequest request) {
        Usuario usuario = authService.cadastrar(request);
        return comTokens(ResponseBuilder.create(Status.CREATED, UsuarioResponse.de(usuario)), sessaoService.iniciar(usuario));
    }

    @POST
    @Path("login")
    public RestResponse<UsuarioResponse> entrar(@Valid LoginRequest request) {
        Usuario usuario = authService.autenticar(request);
        return comTokens(ResponseBuilder.ok(UsuarioResponse.de(usuario)), sessaoService.iniciar(usuario));
    }

    @POST
    @Path("refresh")
    public RestResponse<Void> renovar(@RestCookie(COOKIE_RENOVACAO) String refreshToken) {
        return comTokens(ResponseBuilder.noContent(), sessaoService.renovar(refreshToken));
    }

    @POST
    @Path("logout")
    public RestResponse<Void> sair(@RestCookie(COOKIE_RENOVACAO) String refreshToken) {
        sessaoService.encerrar(refreshToken);
        return ResponseBuilder.<Void>noContent()
                .cookie(cookie(COOKIE_ACESSO, "", PATH_ACESSO, Duration.ZERO),
                        cookie(COOKIE_RENOVACAO, "", PATH_RENOVACAO, Duration.ZERO))
                .build();
    }

    @GET
    @Path("me")
    public UsuarioResponse usuarioAtual() {
        return UsuarioResponse.de(authService.buscar(usuarioLogado.id()));
    }

    private <T> RestResponse<T> comTokens(ResponseBuilder<T> resposta, SessaoService.Tokens tokens) {
        return resposta
                .cookie(cookie(COOKIE_ACESSO, tokens.acesso(), PATH_ACESSO, config.accessToken()),
                        cookie(COOKIE_RENOVACAO, tokens.renovacao(), PATH_RENOVACAO, config.refreshToken()))
                .build();
    }

    private NewCookie cookie(String nome, String valor, String path, Duration validade) {
        return new NewCookie.Builder(nome)
                .value(valor)
                .path(path)
                .maxAge((int) validade.toSeconds())
                .httpOnly(true)
                .secure(config.cookieSecure())
                .sameSite(NewCookie.SameSite.STRICT)
                .build();
    }
}
