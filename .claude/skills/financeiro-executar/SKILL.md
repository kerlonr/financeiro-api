---
name: financeiro-executar
description: Como compilar, rodar, testar e publicar o Lastro (dev local, testes, Docker, deploy no servidor). Use quando precisar subir o projeto, rodar testes, gerar build ou atualizar o servidor.
---

# Executar o Lastro

## Ferramentas

- JDK 17+ (ex.: `~/.jdks/corretto-17.0.20`). No Git Bash: `export JAVA_HOME="$HOME/.jdks/corretto-17.0.20"`.
- Node **22.22+** ou 24 (o Angular 22 recusa versões menores).
- PostgreSQL 16 — via `docker compose -f docker-compose.dev.yml up -d` ou instalado na máquina
  (banco `financeiro`, usuário/senha `financeiro`, porta 5432).

## Desenvolvimento

```bash
cd backend && ./mvnw quarkus:dev        # API em :8080, Dev UI em :8080/q/dev
cd frontend && npm ci && npm start      # interface em :4200 (proxy /api → :8080)
```

## Validação antes de commitar

```bash
cd backend && ./mvnw test               # testes de integração (Dev Services precisa de Docker)
cd frontend && npm test -- --watch=false # testes do frontend (vitest)
cd frontend && npm run build            # compila em modo produção (falha em erro de tipo/template)
npx prettier --check "src/**/*.{ts,html}"
```

Sem Docker para os testes, passe um banco vazio:
`./mvnw test -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/financeiro_test -Dquarkus.datasource.username=... -Dquarkus.datasource.password=...`

## Produção (servidor `server-1`, pasta `~/projects/financeiro-api`)

Publicado em `poo.kerlonr.com.br` pelo túnel Cloudflare, que aponta para `127.0.0.1:8090`.

Primeira vez:

```bash
git pull
cp .env.example .env && nano .env       # senha forte do banco
sudo sh scripts/gerar-chaves.sh
sudo docker compose up -d --build
```

Atualizações: `git pull && sudo docker compose up -d --build`.
O Flyway aplica migrações novas na subida da API.

Conferir: `sudo docker compose ps`, `sudo docker compose logs -f api`.

Atenção: `docker compose down -v` apaga o volume do banco (todos os dados).
