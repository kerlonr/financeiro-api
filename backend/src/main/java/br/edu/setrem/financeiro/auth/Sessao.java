package br.edu.setrem.financeiro.auth;

import br.edu.setrem.financeiro.common.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Um refresh token emitido. Só o hash é salvo; o token real fica apenas no cookie do navegador. */
@Entity
public class Sessao extends EntidadeBase {

    @Column(name = "usuario_id")
    public Long usuarioId;

    @Column(name = "token_hash")
    public String tokenHash;

    public UUID familia;

    @Column(name = "expira_em")
    public Instant expiraEm;

    @Column(name = "revogada_em")
    public Instant revogadaEm;

    public static Optional<Sessao> porHash(String tokenHash) {
        return find("tokenHash", tokenHash).firstResultOptional();
    }

    /**
     * Revoga só se ainda estiver ativa, numa única instrução no banco.
     * Retorna false quando outra requisição já usou este token (mesmo que ao mesmo tempo).
     */
    public static boolean revogar(Long id) {
        return update("revogadaEm = ?1 where id = ?2 and revogadaEm is null", Instant.now(), id) == 1;
    }

    public static void revogarFamilia(UUID familia) {
        update("revogadaEm = ?1 where familia = ?2 and revogadaEm is null", Instant.now(), familia);
    }

    public boolean expirou() {
        return expiraEm.isBefore(Instant.now());
    }
}
