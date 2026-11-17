# Lastro — instruções para o Claude

Aplicação financeira (Quarkus + Angular) da disciplina de POO. Monorepo: `backend/` e `frontend/`.

Leia a skill certa antes de agir:

| Skill | Quando |
|---|---|
| `financeiro-visao-geral` | Entender o sistema, regras de negócio e telas |
| `financeiro-arquitetura` | Antes de criar ou alterar código |
| `financeiro-seguranca` | Ao mexer em auth, endpoints, consultas, deploy; para auditar |
| `financeiro-executar` | Rodar, testar, gerar build, publicar |
| `financeiro-desenvolver` | Fluxo completo de uma mudança com os agentes |

Agentes em `.claude/agents/`: `dev-backend`, `dev-frontend` (desenvolvem), `revisor-seguranca`, `revisor-codigo` (validam, só leitura).

Regras do projeto:
- Manter sempre os requisitos do professor (contas, categorias, transações filtradas por conta e categoria,
  transferência, tela inicial com saldo por conta e geral, JWT em cookie httpOnly).
- Código limpo: sem código morto, sem duplicação (reutilizar os métodos centrais), nomes em português claros e curtos.
- Toda consulta filtra pelo usuário logado.
- Schema só por migração Flyway nova.
- Não commitar `.env` nem `secrets/`.
