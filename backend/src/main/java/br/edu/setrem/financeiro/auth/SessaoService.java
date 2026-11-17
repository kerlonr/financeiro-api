package br.edu.setrem.financeiro.auth;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Emite e renova os tokens de sessão.
 * <ul>
 *   <li>Access token: JWT assinado (RS256), curto, enviado em toda chamada da API.</li>
 *   <li>Refresh token: valor aleatório salvo apenas como hash; trocado por um novo a cada uso (rotação).</li>
 * </ul>
 * Se um refresh token já usado aparecer de novo, alguém copiou o cookie:
 * todas as sessões daquele login (mesma família) são revogadas.
 */
@ApplicationScoped
public class SessaoService {

    private static final SecureRandom GERADOR = new SecureRandom();

    public record Tokens(String acesso, String renovacao) {
    }

    @Inject
    AuthConfig config;

    @Transactional
    public Tokens iniciar(Usuario usuario) {
        return emitir(usuario.id, UUID.randomUUID());
    }

    // Sem rollback na falha: a revogação por reuso precisa ser gravada mesmo com o 401.
    @Transactional(dontRollbackOn = AutenticacaoException.class)
    public Tokens renovar(String refreshToken) {
        Sessao sessao = buscar(refreshToken);

        if (sessao.expirou()) {
            throw sessaoEncerrada();
        }
        if (!Sessao.revogar(sessao.id)) {
            Sessao.revogarFamilia(sessao.familia);
            throw sessaoEncerrada();
        }

        return emitir(sessao.usuarioId, sessao.familia);
    }

    @Transactional
    public void encerrar(String refreshToken) {
        if (refreshToken != null) {
            Sessao.porHash(hash(refreshToken)).ifPresent(sessao -> Sessao.revogarFamilia(sessao.familia));
        }
    }

    private Sessao buscar(String refreshToken) {
        if (refreshToken == null) {
            throw sessaoEncerrada();
        }
        return Sessao.porHash(hash(refreshToken)).orElseThrow(SessaoService::sessaoEncerrada);
    }

    private Tokens emitir(Long usuarioId, UUID familia) {
        String refreshToken = gerarTokenAleatorio();

        Sessao sessao = new Sessao();
        sessao.usuarioId = usuarioId;
        sessao.tokenHash = hash(refreshToken);
        sessao.familia = familia;
        sessao.expiraEm = Instant.now().plus(config.refreshToken());
        sessao.persist();

        String accessToken = Jwt.subject(usuarioId.toString())
                .expiresIn(config.accessToken())
                .sign();

        return new Tokens(accessToken, refreshToken);
    }

    private static String gerarTokenAleatorio() {
        byte[] bytes = new byte[32];
        GERADOR.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM", e);
        }
    }

    private static AutenticacaoException sessaoEncerrada() {
        return new AutenticacaoException("Sua sessão expirou. Entre novamente.");
    }
}
