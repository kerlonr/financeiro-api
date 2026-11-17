package br.edu.setrem.financeiro.auth;

import br.edu.setrem.financeiro.common.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.LockModeType;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Entity
public class Usuario extends EntidadeBase {

    public String nome;
    public String email;

    @Column(name = "senha_hash")
    public String senhaHash;

    @Column(name = "tentativas_falhas")
    public int tentativasFalhas;

    @Column(name = "bloqueado_ate")
    public Instant bloqueadoAte;

    /**
     * Trava a linha até o fim da transação: logins simultâneos com senha errada
     * entram em fila, e nenhuma tentativa deixa de ser contada.
     */
    public static Optional<Usuario> porEmail(String email) {
        return find("email", email).withLock(LockModeType.PESSIMISTIC_WRITE).firstResultOptional();
    }

    public boolean estaBloqueado() {
        return bloqueadoAte != null && bloqueadoAte.isAfter(Instant.now());
    }

    public void registrarFalha(int maxTentativas, Duration bloqueio) {
        tentativasFalhas++;
        if (tentativasFalhas >= maxTentativas) {
            bloqueadoAte = Instant.now().plus(bloqueio);
            tentativasFalhas = 0;
        }
    }

    public void registrarSucesso() {
        tentativasFalhas = 0;
        bloqueadoAte = null;
    }
}
