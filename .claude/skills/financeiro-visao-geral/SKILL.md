---
name: financeiro-visao-geral
description: Explica o que o Lastro (projeto financeiro Quarkus + Angular) faz, as regras de negócio e o fluxo de cada tela. Use para entender o sistema, preparar a apresentação ou responder "como funciona X".
---

# Lastro — o que o sistema faz

Aplicação de finanças pessoais. Cada usuário cadastra as **contas** onde guarda dinheiro, as **categorias**
dos gastos/receitas e lança **transações**. O sistema calcula o saldo de cada conta e o saldo geral.

Requisitos do professor e onde cada um está:

| Requisito | Backend | Frontend |
|---|---|---|
| Lançamento de contas | `conta/ContaResource` | `pages/contas` |
| Categorias | `categoria/CategoriaResource` | `pages/categorias` |
| Transações | `transacao/TransacaoResource` | `pages/transacoes` |
| Filtro por categoria e conta | `TransacaoService.listar` | filtros em `pages/transacoes/transacoes.html` |
| Transferência entre contas | `TransacaoService.transferir` | `pages/transacoes/transferencia-form` |
| Tela inicial com saldo por conta e geral | `painel/PainelService` | `pages/painel` |
| Autenticação JWT em cookie httpOnly | `auth/*` | `pages/auth/*` |

## Regras de negócio

**Usuário**
- E-mail único (guardado em minúsculas). Senha de 8 a 72 caracteres.
- Depois do cadastro o usuário já entra logado.
- 5 senhas erradas seguidas bloqueiam o login por 15 minutos.

**Conta**
- Nome único por usuário (sem diferenciar maiúsculas). Tem `saldoInicial` (pode ser negativo, ex.: cheque especial).
- **Saldo = saldo inicial + entradas − saídas.** Nunca é gravado: é calculado na consulta
  (`ContaService.CONSULTA_SALDOS`), então não existe saldo "desatualizado".
- Conta com transações não pode ser excluída (protege o histórico).

**Categoria**
- Nome único por usuário. Categoria com transações não pode ser excluída.

**Transação**
- Tipo `ENTRADA` ou `SAIDA`, valor sempre positivo com até 2 casas, data sem horário, descrição até 120 caracteres.
- Toda transação comum tem conta e categoria.
- O saldo considera todas as transações, inclusive com data futura.

**Transferência**
- Gera **duas transações** na mesma transação do banco: `SAIDA` na origem e `ENTRADA` no destino,
  ligadas pelo mesmo `transferenciaId` (UUID) e **sem categoria**.
- Origem e destino precisam ser diferentes e do mesmo usuário.
- Sem descrição, cada lado recebe "Transferência para X" / "Transferência de Y".
- Não pode ser editada (só excluída). Excluir um lado exclui os dois — o dinheiro nunca "some".
- Transferências **não contam** nos totais de entradas/saídas do mês, porque o dinheiro só mudou de conta.

Regra de banco que garante isso: `CHECK ((transferencia_id IS NULL) = (categoria_id IS NOT NULL))`
em `V1__schema_inicial.sql`.

## Telas

| Rota | Tela | O que mostra |
|---|---|---|
| `/login`, `/cadastro` | Entrada | Formulário + painel escuro com amostra de saldos |
| `/` | Início | Saldo geral, entradas/saídas do mês, saldo de cada conta, últimas 6 transações, atalhos para lançar e transferir |
| `/transacoes` | Transações | Navegação por mês, filtros de conta e categoria, totais do período, lista agrupada por dia, editar/excluir |
| `/contas` | Contas | Lista com saldo atual e inicial, criar/editar/excluir |
| `/categorias` | Categorias | Lista, criar/editar/excluir |

Formulários abrem em diálogo (Angular Material). Exclusões sempre pedem confirmação.

## Fluxo de uma requisição (exemplo: lançar uma saída)

1. `TransacaoForm.salvar()` monta o `TransacaoRequest` e chama `EnvioFormulario.enviar`.
2. `TransacaoService.criar` faz `POST /api/transacoes` (o cookie `access_token` vai junto automaticamente).
3. No servidor, o SmallRye JWT valida o token do cookie; `UsuarioLogado.id()` lê o usuário.
4. `TransacaoResource.criar` valida o corpo (`@Valid`) e chama `TransacaoService.criar`.
5. O service busca conta e categoria **filtrando pelo usuário** (de outro usuário → 404) e grava.
6. Volta `201`; o diálogo fecha, aparece "Salvo." e a lista recarrega (`rxResource.reload()`).

Se o access token venceu (15 min), a API responde 401, o `sessaoInterceptor` chama `/api/auth/refresh`
e repete a requisição sem o usuário perceber.

Para detalhes de código veja a skill `financeiro-arquitetura`; de segurança, `financeiro-seguranca`.
