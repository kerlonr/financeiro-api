---
name: financeiro-arquitetura
description: Arquitetura e convenções de código do Lastro (backend Quarkus em package-by-feature, frontend Angular em pages/<funcionalidade>). Use antes de criar ou alterar qualquer código do projeto, ou para explicar onde cada coisa fica.
---

# Arquitetura do Lastro

Monorepo: `backend/` (Quarkus) e `frontend/` (Angular). Em produção o nginx serve o Angular e repassa
`/api` ao Quarkus — **mesma origem**, por isso os cookies `SameSite=Strict` funcionam sem CORS.

```
navegador ──► nginx (web) ──► /api ──► Quarkus (api) ──► PostgreSQL (db)
                 └─ arquivos do Angular
```

## Backend — organização por funcionalidade

Pacote base `br.edu.setrem.financeiro`. Cada pasta é uma funcionalidade completa:

| Arquivo | Papel | Regra |
|---|---|---|
| `XResource` | Camada HTTP | Só recebe, valida (`@Valid`) e delega. Sem regra de negócio. |
| `XService` | Regras de negócio | `@Transactional` aqui. Sempre filtra pelo `UsuarioLogado`. |
| `X` (entidade) | Tabela | Panache active record, campos públicos, estende `EntidadeBase`. |
| `XRequest` | Entrada da API | `record` com Bean Validation e mensagens em português. |
| `XResponse` | Saída da API | `record`; nunca devolver a entidade direto (vazaria `usuarioId`). |

`common/` tem só o que é usado por todas as funcionalidades:
- `EntidadeBase` — id `IDENTITY` comum.
- `UsuarioLogado` — id do usuário vindo do JWT (`sub`).
- `RegraNegocioException` → 422; `TratadorErros` converte exceções em `ErroResponse`.

Métodos centrais que devem ser **reutilizados** (não reescrever a consulta):
- `ContaService.buscar(id)` / `CategoriaService.buscar(id)` — busca do usuário logado ou 404.
- `ContaService.listarComSaldo()` — única fonte do saldo.
- `TransacaoService.total(tipo, inicio, fim)` — somatório sem transferências.

Status HTTP: `201` criar, `204` atualizar/excluir, `400` validação, `401` sessão, `404` inexistente
ou de outro usuário, `422` regra de negócio.

Banco: **somente Flyway** cria/altera tabelas (`db/migration/V<n>__descricao.sql`). O Hibernate roda em
`validate` e derruba o boot se entidade e tabela divergirem. Nunca editar uma migração já aplicada.

Dinheiro: `BigDecimal` no Java, `NUMERIC(14,2)` no banco, `@Digits(integer = 12, fraction = 2)` na entrada.

## Frontend — estrutura pedida pelo professor

```
src/app/
  common/                         reutilizável por qualquer página
    crud-api.ts                   listar/criar/atualizar/excluir genérico
    envio-formulario.ts           validar → enviar → erro (todos os formulários)
    dialogos.service.ts           abrir formulário em diálogo / confirmar exclusão
    erro-api.ts, datas.ts         mensagens de erro e datas "AAAA-MM-DD"
    layout/, confirmacao/         moldura com menu e diálogo de confirmação
  pages/<funcionalidade>/
    model/                        interfaces (espelham os records do backend)
    services/                     chamadas HTTP (HttpClient)
    <funcionalidade>.ts/.html/.css   página
    <x>-form/                     diálogo de formulário
```

Regras:
- **Signals** para estado; `rxResource` para listas que recarregam (`.value()`, `.isLoading()`, `.reload()`).
- **Reactive forms** com `NonNullableFormBuilder`; validações iguais às do backend.
- **Angular Material** para componentes; HTML e CSS em arquivos separados; CSS puro (sem Sass).
- Tema em `src/styles.css`: tokens `--cor-*` e sobrescrita das variáveis `--mat-sys-*`.
  Classes globais reutilizáveis: `.pagina`, `.cartao`, `.linha-lista`, `.icone-linha`, `.valor`, `.entrada`, `.saida`, `.vazio`, `.formulario`.
- URLs relativas (`/api/...`); em dev o `proxy.conf.json` manda para `localhost:8080`.
- Datas: sempre `paraDataIso`/`deDataIso` (nunca `toISOString`, que muda o dia por causa do fuso).
- Rotas com `loadComponent` (cada página é um pacote separado).

## Como adicionar uma funcionalidade (ex.: "metas")

1. Migração `V2__metas.sql`.
2. Backend `meta/`: `Meta`, `MetaRequest`, `MetaResponse`, `MetaService` (com `UsuarioLogado`), `MetaResource`.
3. Teste em `src/test/java/.../MetaTest.java`, incluindo isolamento entre usuários.
4. Frontend `pages/metas/{model,services}`, `MetaService extends CrudApi`, página e `meta-form`.
5. Rota em `app.routes.ts` e item em `common/layout/layout.ts`.
6. Rodar a skill `financeiro-desenvolver` (revisões de código e segurança).

## Estilo de código

- Nomes em português, claros e curtos (`contaService`, `saldoInicial`, `transferenciaId`).
- Um método por responsabilidade; se dois lugares precisam da mesma lógica, ela vira um método central.
- Sem código morto, sem métodos "para o futuro", sem comentários óbvios — comentário só explica o **porquê**.
