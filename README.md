# Lastro · Gestão financeira pessoal

Aplicação de finanças pessoais feita com **Quarkus** (API) e **Angular** (interface), com autenticação JWT em cookie httpOnly.

Trabalho da disciplina de Programação Orientada a Objetos — Prof. Tiago Seibel.

## Visão Geral

O projeto permite:

- criar uma conta de usuário e entrar com e-mail e senha
- cadastrar **contas** (corrente, poupança, carteira...) com saldo inicial
- cadastrar **categorias** (mercado, salário, transporte...)
- lançar **transações** de entrada e saída
- filtrar transações por **mês**, **conta** e **categoria**
- **transferir** valores entre contas
- ver na **tela inicial** o saldo de cada conta, o saldo geral e o resumo do mês

Cada usuário só enxerga os próprios dados.

## Stack

- Java 17 + Quarkus 3 (REST, Hibernate ORM com Panache, Hibernate Validator, SmallRye JWT, Flyway)
- PostgreSQL 16
- Angular 22 + Angular Material (signals, reactive forms, CSS puro)
- nginx (produção) e Docker Compose

## Estrutura

```text
.
|-- backend/                      API Quarkus
|   |-- src/main/java/br/edu/setrem/financeiro/
|   |   |-- auth/                 cadastro, login, sessão (JWT + refresh token)
|   |   |-- conta/                contas e cálculo de saldo
|   |   |-- categoria/            categorias
|   |   |-- transacao/            transações, filtros e transferências
|   |   |-- painel/               resumo da tela inicial
|   |   `-- common/               base das entidades, usuário logado, tratamento de erros
|   |-- src/main/resources/
|   |   |-- application.properties
|   |   `-- db/migration/         scripts do banco (Flyway)
|   |-- src/test/java/            testes de integração
|   `-- Dockerfile
|-- frontend/                     Interface Angular
|   |-- src/app/
|   |   |-- common/               componentes e utilitários reutilizáveis
|   |   `-- pages/<funcionalidade>/{model, services, componentes}
|   |-- nginx.conf                servidor de produção (headers de segurança + proxy da API)
|   `-- Dockerfile
|-- scripts/gerar-chaves.sh       gera as chaves RSA do JWT para produção
|-- docker-compose.yml            aplicação completa (produção)
|-- docker-compose.dev.yml        apenas o banco, para desenvolvimento
`-- .env.example
```

A documentação detalhada (arquitetura, segurança, regras de negócio) fica em `.claude/skills/`.

## Requisitos

| Para                | Precisa                                   |
|---------------------|-------------------------------------------|
| Rodar em modo dev   | JDK 17+, Node 22.22+ (ou 24), PostgreSQL 16 |
| Rodar tudo em Docker| Docker com Compose                        |

O Maven não precisa ser instalado: o projeto usa o wrapper `./mvnw`.

## Rodando em desenvolvimento

1. **Banco de dados** — suba um PostgreSQL com banco `financeiro`, usuário `financeiro` e senha `financeiro` em `localhost:5432`.
   Com Docker, basta:

   ```bash
   docker compose -f docker-compose.dev.yml up -d
   ```

2. **API** (porta 8080) — as tabelas são criadas automaticamente pelo Flyway:

   ```bash
   cd backend
   ./mvnw quarkus:dev
   ```

   No Windows PowerShell use `.\mvnw.cmd quarkus:dev`.

3. **Interface** (porta 4200) — o `proxy.conf.json` repassa `/api` para a porta 8080:

   ```bash
   cd frontend
   npm ci
   npm start
   ```

4. Acesse **http://localhost:4200**, clique em **Criar conta** e comece.

Em modo dev o Quarkus gera sozinho as chaves do JWT; não é preciso configurar nada.

## Rodando com Docker (produção)

```bash
cp .env.example .env              # e troque a senha do banco
sudo sh scripts/gerar-chaves.sh   # cria secrets/jwt-privada.pem e secrets/jwt-publica.pem
docker compose up -d --build
```

A aplicação fica em **http://127.0.0.1:8090** (porta definida por `PORTA` no `.env`).
Só o nginx é publicado, e apenas em `127.0.0.1`; banco e API ficam na rede interna do Docker.
Para acesso externo use um proxy reverso com HTTPS (no servidor: túnel Cloudflare).

## Testes

Os testes de integração ficam em `backend/src/test` e sobem um PostgreSQL temporário automaticamente (Quarkus Dev Services, exige Docker):

```bash
cd backend
./mvnw test
```

Sem Docker, aponte para um PostgreSQL vazio:

```bash
./mvnw test -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/financeiro_test \
            -Dquarkus.datasource.username=postgres -Dquarkus.datasource.password=postgres
```

Cobrem: autenticação (cookies, bloqueio por tentativas, rotação e reuso de refresh token, logout),
isolamento entre usuários, cálculo de saldo, transferências, filtros e validações.

Interface (testes dos utilitários de data e de mensagens de erro, e build de produção):

```bash
cd frontend
npm test -- --watch=false
npm run build
```

## Endpoints

Todos sob `/api`. Exceto as rotas de entrada, todos exigem o cookie de sessão.

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/auth/cadastro` | Cria usuário e já inicia a sessão |
| `POST` | `/auth/login` | Entra com e-mail e senha |
| `POST` | `/auth/refresh` | Renova a sessão (troca o refresh token) |
| `POST` | `/auth/logout` | Encerra a sessão |
| `GET` | `/auth/me` | Usuário logado |
| `GET` | `/painel` | Saldo geral, saldo por conta, totais do mês e últimas transações |
| `GET` `POST` | `/contas` | Lista (com saldo) / cria |
| `PUT` `DELETE` | `/contas/{id}` | Atualiza / exclui |
| `GET` `POST` | `/categorias` | Lista / cria |
| `PUT` `DELETE` | `/categorias/{id}` | Atualiza / exclui |
| `GET` | `/transacoes?inicio=&fim=&contaId=&categoriaId=` | Lista do período (obrigatório, até 1 ano); conta e categoria opcionais |
| `POST` | `/transacoes` | Lança entrada ou saída |
| `PUT` `DELETE` | `/transacoes/{id}` | Atualiza / exclui |
| `POST` | `/transacoes/transferencia` | Transfere entre duas contas |

Erros sempre voltam no formato `{ "mensagem": "...", "campos": { "campo": "..." } }`.

## Segurança

- Senhas com **bcrypt** (custo 12); mesma resposta e mesmo tempo para e-mail inexistente ou senha errada.
- Conta bloqueada por 15 minutos após 5 senhas erradas; nginx limita as rotas de login por IP.
- **Access token JWT (RS256) de 15 minutos** em cookie `httpOnly`, `Secure`, `SameSite=Strict`.
- **Refresh token** aleatório de 8 horas, guardado no banco só como hash SHA-256, **trocado a cada uso**;
  se um token já usado reaparecer, todas as sessões daquele login são derrubadas.
- Toda consulta filtra pelo usuário do token: acessar o id de outro usuário retorna 404.
- Validação em duas camadas (Bean Validation na API + `CHECK`/`FOREIGN KEY` no banco).
- Front e API na mesma origem (sem CORS), CSP restrita, `X-Frame-Options`, HSTS e demais headers no nginx.
- Contêineres sem root, sistema de arquivos somente leitura, sem capabilities; segredos fora do git.

## Boas práticas para este repositório

- não commitar `.env` nem a pasta `secrets/`
- mudanças no banco sempre por um novo arquivo `V<n>__descricao.sql` (nunca editar uma migração já aplicada)
- rodar `./mvnw test` e `npm run build` antes de subir
