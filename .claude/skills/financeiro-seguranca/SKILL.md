---
name: financeiro-seguranca
description: Modelo de segurança do Lastro (JWT em cookie httpOnly, refresh token com rotação, isolamento por usuário, headers, Docker) e checklist de revisão. Use ao mexer em autenticação, endpoints, consultas, deploy, ou para auditar o projeto.
---

# Segurança do Lastro

## Autenticação

| Item | Como funciona | Onde |
|---|---|---|
| Senha | bcrypt custo 12; 8–72 caracteres | `AuthService` |
| E-mail inexistente | verifica contra um hash fictício: mesma mensagem e mesmo tempo da senha errada | `AuthService.HASH_FICTICIO` |
| Força bruta | 5 erros → bloqueio de 15 min (gravado mesmo com o 401: `dontRollbackOn`). A linha do usuário é travada (`PESSIMISTIC_WRITE`) para logins simultâneos não "perderem" tentativas. nginx: 20 req/min por IP em `/api/auth/` | `Usuario.porEmail`, `Usuario.registrarFalha`, `nginx.conf` |
| Access token | JWT RS256, 15 min, só `sub` (id). Cookie `access_token`, `Path=/api` | `SessaoService.emitir` |
| Refresh token | 32 bytes aleatórios, 8 h. Cookie `refresh_token`, `Path=/api/auth`. Banco guarda só SHA-256 | `Sessao`, `SessaoService` |
| Rotação | cada `/refresh` revoga o token usado e emite outro na mesma "família". A revogação é um `UPDATE ... WHERE revogada_em IS NULL`: duas chamadas simultâneas não conseguem usar o mesmo token | `Sessao.revogar`, `SessaoService.renovar` |
| Reuso | token já revogado reapresentado ⇒ revoga a família inteira (cookie roubado) | `SessaoService.renovar` |
| Logout | revoga a família no banco e expira os dois cookies | `AuthResource.sair` |
| Cookies | `HttpOnly` (JS não lê), `Secure` (só HTTPS; desligado só em dev/test), `SameSite=Strict` (CSRF) | `AuthResource.cookie` |
| Chaves JWT | dev/test: geradas pelo Quarkus. Produção: `secrets/*.pem` via Docker secrets, fora do git | `scripts/gerar-chaves.sh` |

O frontend nunca vê tokens: só sabe quem está logado (`AuthService.usuario`). Várias requisições com 401
compartilham **uma** renovação (`renovarSessao` + `shareReplay`), senão a segunda seria tratada como reuso.

## Autorização

- Negado por padrão: `quarkus.http.auth.permission.api.paths=/api/*` exige login; só login, cadastro,
  refresh e logout são públicos.
- `quarkus.http.auth.proactive=false`: cookie vencido não bloqueia login/refresh.
- **Isolamento por usuário (anti-IDOR)**: toda consulta tem `usuarioId = UsuarioLogado.id()`.
  Recurso de outro usuário responde **404** (não 403, para não confirmar que existe).
  Coberto por `IsolamentoTest`.

## Entrada de dados

- DTOs `record` (sem mass assignment: o cliente não consegue setar `id`, `usuarioId`, `transferenciaId`).
- Bean Validation com limites de tamanho; corpo máximo de 64 KB (Quarkus e nginx).
- Consultas sempre parametrizadas (Panache/JPQL com `:parametro`), nunca concatenando valor do usuário.
- Banco reforça: `CHECK (valor > 0)`, `CHECK tipo`, `UNIQUE (usuario_id, nome)` e chaves estrangeiras
  **compostas** `(conta_id, usuario_id)` / `(categoria_id, usuario_id)`: mesmo com um bug no Java, o banco recusa
  uma transação ligada à conta ou categoria de outro usuário.
- Listagem de transações exige período de no máximo 1 ano (evita carregar o histórico inteiro de uma vez).
- Erros não vazam stack trace: `TratadorErros` devolve só `mensagem`/`campos`.

## Navegador e infraestrutura

- nginx: CSP `default-src 'self'` (sem CDN, fontes empacotadas), `frame-ancestors 'none'`, HSTS,
  `nosniff`, `Referrer-Policy: no-referrer`, `Permissions-Policy`, `server_tokens off`.
- Build Angular com `inlineCritical: false` (o CSS crítico inline usa `onload=`, que a CSP bloquearia).
- Docker: só o nginx publicado, em `127.0.0.1`; API e banco na rede interna; usuário não-root;
  `read_only`, `cap_drop: ALL`, `no-new-privileges`. Senha do banco no `.env` (fora do git).
- IP real do cliente vem de `CF-Connecting-IP`, aceito apenas da rede local/Docker.

## Limitações conhecidas (documentar na apresentação)

- O cadastro informa "e-mail já cadastrado" (enumeração de e-mails), escolha de usabilidade comum.
- O bloqueio por tentativas é por conta: um atacante pode bloquear temporariamente a conta de alguém.
- Duas abas renovando exatamente ao mesmo tempo podem disparar a detecção de reuso (usuário volta ao login).
- O JWT de acesso é stateless: após logout ou revogação ele ainda vale até expirar (no máximo 15 min).
  O cookie é apagado no logout, então isso só importa se o token tiver sido copiado antes.
- A API conecta ao Postgres com o usuário criado pela imagem oficial, que é superusuário. O banco não é exposto
  fora da rede interna; um papel sem privilégios de superusuário seria o próximo passo de endurecimento.

## Checklist de revisão (use em toda mudança)

1. Novo endpoint está sob `/api` e **não** foi adicionado à lista pública?
2. Toda consulta nova filtra por `usuarioId`? Busca por id usa `buscar(id)` do service?
3. Entrada é um `record` com `@Valid` e limites de tamanho? Valores em `BigDecimal` com `@Digits`?
4. Resposta é um `record` (sem expor entidade, hash, `usuarioId`)?
5. Mudança de schema via nova migração Flyway, com constraints?
6. Nada de token, senha ou dado pessoal em log?
7. Frontend: nada em `localStorage`, nada de `innerHTML`/`bypassSecurityTrust*`, URLs relativas?
8. Novo segredo vai para `.env`/`secrets/` e `.env.example` documenta o nome?
9. Teste cobre o caminho feliz **e** o acesso de outro usuário?
