# ADR 0002 — Monorepo poliglota sem Turborepo

**Status:** Accepted  
**Date:** 2026-06-02

## Context

O projeto tem dois apps com toolchains completamente diferentes: Java/Maven (backend) e Node/npm (frontend). Turborepo e pnpm workspaces são ferramentas de orquestração orientadas a projetos Node. Forçar o Maven dentro desse modelo seria artificioso e introduziria complexidade sem benefício.

## Decision

Monorepo poliglota **sem** orquestrador de build central. Cada app é construído e rodado independentemente:

- `apps/api` → Maven (`./mvnw`)
- `apps/web` → npm

`docs/`, `CLAUDE.md`, `AGENTS.md` e `.github/` ficam na raiz e são compartilhados pelos dois apps.

## Consequences

- **Simples:** nenhuma configuração de workspace, nenhum `turbo.json`.
- **Independência:** um app não bloqueia o build do outro.
- **Trade-off:** não há `pnpm dev` na raiz que suba os dois — precisa de dois terminais. Aceitável no contexto de estudo; se necessário, um `Makefile` ou script shell pode orquestrar (issue futura).