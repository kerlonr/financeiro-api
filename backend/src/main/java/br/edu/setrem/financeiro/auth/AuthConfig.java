package br.edu.setrem.financeiro.auth;

import io.smallrye.config.ConfigMapping;

import java.time.Duration;

/** Parâmetros de autenticação definidos em application.properties (prefixo "financeiro.auth"). */
@ConfigMapping(prefix = "financeiro.auth")
public interface AuthConfig {

    /** Validade do JWT de acesso. Curta de propósito: se vazar, expira rápido. */
    Duration accessToken();

    /** Validade do refresh token. Renovada a cada uso (rotação). */
    Duration refreshToken();

    /** Cookies só trafegam em HTTPS. Desligado apenas em desenvolvimento. */
    boolean cookieSecure();

    /** Tentativas de senha erradas antes de bloquear a conta temporariamente. */
    int maxTentativas();

    Duration bloqueio();
}
