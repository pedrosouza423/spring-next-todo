# `spring-next-todo` — Documentation Index

Bem-vindo. Este diretório é a **biblioteca de referência durável** do projeto.
Para comandos do dia-a-dia e convenções operacionais, veja [CLAUDE.md](../CLAUDE.md) na raiz.

## How to navigate

A documentação está organizada em **três camadas semânticas**:

| Camada | Pasta | Quando ler | Quando atualizar |
| --- | --- | --- | --- |
| **Evergreen** | [architecture/](architecture/) | Para entender "o que o projeto É hoje" | Quando a arquitetura muda — sobrescrever no lugar (sem datar) |
| **Time-bound** | [specs/](specs/) | Para entender "o que foi decidido neste PR" — snapshot do design | Um por feature/PR significativo; **nunca editar** depois de implementado |
| **Decisões** | [adr/](adr/) | Para entender "por que escolhemos X e não Y" | Quando uma decisão arquitetural surgir ou for re-questionada |

> **Regra prática:** verdade atual → `architecture/`. Contexto histórico → `specs/` ou `adr/`.

## Architecture (evergreen)

- [architecture/monorepo-overview.md](architecture/monorepo-overview.md) — mapa do sistema: apps, portas, dependências, convenções
- [architecture/domain-model.md](architecture/domain-model.md) — ERD da entidade `Task`
- [architecture/api-routes.md](architecture/api-routes.md) — referência de todos os endpoints: contratos, status codes, error envelope
- [architecture/local-development.md](architecture/local-development.md) — setup local: Java, Node, comandos por workflow

## Specs (time-bound)

| Spec | Status | PR |
| --- | --- | --- |
| [2026-06-02-mvp-task-crud.md](specs/2026-06-02-mvp-task-crud.md) | Implemented | — |
| [2026-06-04-auth-users.md](specs/2026-06-04-auth-users.md) | Implemented | feat/auth-jwt |

Template para novos specs: [specs/_template.md](specs/_template.md).

## ADRs (architectural decision records)

| ADR | Decisão | Status |
| --- | --- | --- |
| [0001-h2-over-postgres-for-mvp.md](adr/0001-h2-over-postgres-for-mvp.md) | H2 em memória no MVP | Accepted |
| [0002-polyglot-monorepo-no-turbo.md](adr/0002-polyglot-monorepo-no-turbo.md) | Monorepo poliglota sem Turborepo | Accepted |

Veja [adr/README.md](adr/README.md) para o template.

## Plans

[plans/](plans/) guarda planos de execução de specs aprovados — descartáveis após o merge do PR correspondente.

## Backlog — Docs not yet written

| Doc | Quando criar |
| --- | --- |
| `architecture/auth-flow.md` | Diagrama de sequência do fluxo JWT (issue #1 implementada, doc pendente) |
| `architecture/deployment.md` | Quando o primeiro pipeline CI/CD de deploy subir |