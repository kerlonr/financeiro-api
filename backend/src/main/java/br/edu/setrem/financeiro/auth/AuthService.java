package br.edu.setrem.financeiro.auth;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;
import br.edu.setrem.financeiro.common.RegraNegocioException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.Locale;

@ApplicationScoped
public class AuthService {

    private static final int CUSTO_BCRYPT = 12;
    private static final BCrypt.Version VERSAO_BCRYPT = BCrypt.Version.VERSION_2A;
    private static final BCrypt.Hasher HASHER =
            BCrypt.with(VERSAO_BCRYPT, LongPasswordStrategies.truncate(VERSAO_BCRYPT));
    private static final BCrypt.Verifyer VERIFICADOR =
            BCrypt.verifyer(VERSAO_BCRYPT, LongPasswordStrategies.truncate(VERSAO_BCRYPT));

    // Usado quando o e-mail não existe: a verificação leva o mesmo tempo,
    // então não dá para descobrir quais e-mails estão cadastrados medindo a resposta.
    private static final String HASH_FICTICIO = gerarHash("senha-ficticia");

    @Inject
    AuthConfig config;

    @Transactional
    public Usuario cadastrar(CadastroRequest request) {
        String email = normalizarEmail(request.email());
        if (Usuario.porEmail(email).isPresent()) {
            throw new RegraNegocioException("Já existe uma conta com este e-mail.");
        }

        Usuario usuario = new Usuario();
        usuario.nome = request.nome().trim();
        usuario.email = email;
        usuario.senhaHash = gerarHash(request.senha());
        usuario.persist();
        return usuario;
    }

    // Sem rollback na falha: o contador de tentativas precisa ser gravado mesmo com o 401.
    @Transactional(dontRollbackOn = AutenticacaoException.class)
    public Usuario autenticar(LoginRequest request) {
        Usuario usuario = Usuario.porEmail(normalizarEmail(request.email())).orElse(null);
        String hash = usuario != null ? usuario.senhaHash : HASH_FICTICIO;
        boolean senhaCorreta = VERIFICADOR.verify(request.senha().toCharArray(), hash).verified;

        if (usuario == null) {
            throw credenciaisInvalidas();
        }
        if (usuario.estaBloqueado()) {
            throw new AutenticacaoException("Muitas tentativas sem sucesso. Tente novamente em alguns minutos.");
        }
        if (!senhaCorreta) {
            usuario.registrarFalha(config.maxTentativas(), config.bloqueio());
            throw credenciaisInvalidas();
        }

        usuario.registrarSucesso();
        return usuario;
    }

    public Usuario buscar(Long id) {
        return Usuario.<Usuario>findByIdOptional(id).orElseThrow(NotFoundException::new);
    }

    private static String gerarHash(String senha) {
        return HASHER.hashToString(CUSTO_BCRYPT, senha.toCharArray());
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static AutenticacaoException credenciaisInvalidas() {
        return new AutenticacaoException("E-mail ou senha incorretos.");
    }
}
