---
name: financeiro-desenvolver
description: Fluxo para implementar e validar qualquer mudança no Lastro usando os agentes do projeto (dev-backend, dev-frontend, revisor-seguranca, revisor-codigo). Use ao receber um pedido de nova funcionalidade, correção ou refatoração.
---

# Fluxo de desenvolvimento

Toda mudança passa por **desenvolver → testar → revisar → corrigir**. Nada é considerado pronto
sem as duas revisões aprovadas.

## 1. Entender

- Ler as skills `financeiro-visao-geral` e `financeiro-arquitetura`.
- Confirmar que a mudança mantém os requisitos do professor (tabela em `financeiro-visao-geral`).

## 2. Desenvolver

| Parte | Agente |
|---|---|
| API, banco, testes de integração | `dev-backend` |
| Telas, services, formulários, estilo | `dev-frontend` |

Mudanças que tocam os dois lados: primeiro o backend (contrato da API), depois o frontend,
com o `model/` do Angular espelhando os `record` do Java.

## 3. Testar

```bash
cd backend && ./mvnw test
cd frontend && npm run build
```

Os dois precisam passar. Teste novo para toda regra nova, incluindo o acesso de outro usuário.

## 4. Revisar (em paralelo)

- `revisor-seguranca` — usa o checklist da skill `financeiro-seguranca`.
- `revisor-codigo` — arquitetura, duplicação, código morto, legibilidade.

Os revisores **não editam**; devolvem achados com arquivo:linha, gravidade e correção sugerida.

## 5. Corrigir e repetir

Corrigir os achados com o agente de desenvolvimento correspondente, rodar os testes de novo e pedir
nova revisão apenas do que mudou. Encerrar quando não houver achado de gravidade alta ou média.

## 6. Documentar

Se a mudança altera regra de negócio, endpoint, segurança ou forma de rodar, atualizar a skill
correspondente e o `README.md` no mesmo commit.
