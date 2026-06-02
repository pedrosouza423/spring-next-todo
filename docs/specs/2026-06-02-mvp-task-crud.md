# Spec — MVP Task CRUD

**Date:** 2026-06-02  
**Status:** Implemented — branch `main` (bootstrap)  
**Authors:** Pedro + Lucas

## Problem

Precisamos de um ponto de partida funcional: um To-Do app que mostre os dois lados da stack (Spring Boot + Next.js) se comunicando ponta a ponta, sem complexidade de auth ou multi-tenant.

## Solution overview

CRUD completo da entidade `Task` — criação, listagem, edição, toggle de concluída e remoção. Backend em Spring Boot com H2 em memória; frontend em Next.js com dark theme e mobile-first usando shadcn/ui.

## Approach considered

**H2 em memória vs PostgreSQL:** escolhemos H2 para eliminar a dependência de Docker no setup inicial. Migração para Postgres é planejada (issue #6). Ver [ADR 0001](../adr/0001-h2-over-postgres-for-mvp.md).

**Monorepo vs repos separados:** monorepo poliglota para compartilhar `docs/` e manter front + back versionados juntos. Ver [ADR 0002](../adr/0002-polyglot-monorepo-no-turbo.md).

## Backend changes

- Entidade `Task` com JPA Auditing (`createdAt`, `updatedAt` automáticos)
- `TaskRepository` com derived query `findAllByOrderByCreatedAtDesc`
- `TaskService` com camada de negócio isolada do HTTP
- `TaskController` com Bean Validation nos requests
- `GlobalExceptionHandler` com error envelope consistente (`ApiError`)
- CORS liberado para `localhost:3000`
- SeedData com 3 tarefas de exemplo

## Frontend changes

- `lib/api.ts` — client fetch tipado (sem biblioteca externa)
- `app/page.tsx` — Server Component que faz fetch inicial
- `TaskList` — Client Component com estado local
- `TaskItem` — item com toggle, edit e delete
- `TaskForm` — formulário de criação inline
- `TaskEditDialog` — modal de edição via shadcn Dialog
- Dark theme forçado via classe `dark` no `<html>`

## Out of scope

Auth, categorias, prazos, prioridades, filtros, PostgreSQL/Docker — todos registrados como issues.

## Verification

1. `./mvnw spring-boot:run` → `POST /tasks` cria, `GET /tasks` lista, `PATCH /{id}/toggle` alterna, `DELETE` remove
2. `npm run dev` → página carrega em `:3000` com tema dark
3. Integração: criar/concluir/editar/deletar tarefa pela UI com ambos os serviços rodando