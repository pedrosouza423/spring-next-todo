# Spec — Listas compartilhadas / colaboração (Issue #6)

**Status:** Approved — branch `feat/shared-lists`
**Date:** 2026-06-08

## Problem

Pedro e Lucas usam o app juntos e não conseguem compartilhar tarefas entre si. Hoje cada `Task` pertence diretamente a um único `User` — não existe grouping nem permissões granulares. Quem quiser colaborar precisa duplicar tarefas manualmente.

## Solution overview

Introduzimos a entidade `TaskList` como agrupamento de tarefas. Cada lista tem membros com papéis (OWNER, EDITOR, VIEWER). Toda tarefa passa a pertencer a uma lista. Convite funciona via e-mail de usuário já cadastrado ou link com token (sem SMTP), 100% no app.

## Approach considered

| Alternativa | Decisão |
|-------------|---------|
| Compartilhar Task avulsa (sem entidade de lista) | Descartado — não cobre o caso de uso "lista compartilhada"; gera atrito de uso |
| Lista opcional (tasks planas + listas separadas) | Descartado — dois caminhos de código duplicados, mais complexidade de manutenção |
| **TaskList com membership obrigatória** | **Escolhido** — modelo limpo, extensível, espelha o padrão `category/` do projeto |
| Convite por SMTP | Descartado — sem infra de e-mail no ambiente dev H2 in-memory |

## Data model

Novos tipos no pacote `com.springnexttodo.tasklist`:

```
TaskList
  id, name (@NotBlank max 255), owner (User FK), createdAt, updatedAt

TaskListMember
  id, taskList (FK), user (FK), role (ListRole), createdAt
  UNIQUE (task_list_id, user_id)

ListInvite
  id, taskList (FK), token (unique, SecureRandom+Base64URL), role, email (nullable),
  createdBy (User FK), expiresAt, acceptedAt (nullable), acceptedBy (nullable), createdAt

ListRole (enum)
  OWNER > EDITOR > VIEWER
```

Alteração em `Task`: adiciona campo `taskList (FK not-null)` mantendo `user` como criador.

## Authorization rules

- Não-membro → 404 (evita leak de existência da lista)
- Membro sem papel suficiente → 403
- VIEWER: lê tarefas
- EDITOR: cria/edita/toggle/exclui tarefas na lista
- OWNER: tudo acima + gerenciar membros, convites, renomear, excluir lista

## Backend changes

### Novos arquivos

```
tasklist/
  TaskList.java
  TaskListMember.java
  ListRole.java (enum)
  ListInvite.java
  TaskListRepository.java
  TaskListMemberRepository.java
  ListInviteRepository.java
  TaskListService.java
  MemberService.java
  InviteService.java
  ListAccessService.java      ← centraliza RBAC (requireMembership, requireRole)
  TaskListController.java     ← /lists + /lists/{id}/members + /lists/{id}/invites
  InviteController.java       ← /invites/{token} e /invites/{token}/accept
  dto/
    TaskListRequest.java      — name
    TaskListResponse.java     — id, name, role, memberCount, taskCount, createdAt
    MemberRequest.java        — email, role
    MemberResponse.java       — userId, name, email, role
    RoleUpdateRequest.java    — role
    InviteRequest.java        — role, email?
    InviteResponse.java       — token, role, listName, expiresAt
common/
  ForbiddenException.java
```

### Arquivos modificados

| Arquivo | Mudança |
|---------|---------|
| `task/Task.java` | Adiciona `taskList` (@ManyToOne not-null) |
| `task/TaskRepository.java` | `findFiltered` filtra por membership; aceita `listId` opcional |
| `task/TaskService.java` | Operações de escrita requerem EDITOR+; leitura requer VIEWER+ |
| `task/dto/TaskRequest.java` | Campo opcional `listId` |
| `task/dto/TaskResponse.java` | Campo `listId` |
| `task/TaskController.java` | `?listId` no GET /tasks |
| `config/SeedData.java` | Lista padrão + lista compartilhada de exemplo |
| `auth/AuthService.java` | Cria lista padrão "Minhas Tarefas" no register |
| `common/GlobalExceptionHandler.java` | Handler 403 para ForbiddenException |

### Endpoints

| Método | Path | Papel mínimo | Ação |
|--------|------|--------------|------|
| GET | `/lists` | logado | Lista todas as listas do usuário |
| POST | `/lists` | logado | Cria lista (criador vira OWNER) |
| GET | `/lists/{id}` | VIEWER | Detalhe da lista + membros |
| PUT | `/lists/{id}` | OWNER | Renomear |
| DELETE | `/lists/{id}` | OWNER | Excluir (em cascata membros/convites/tarefas) |
| GET | `/lists/{id}/members` | VIEWER | Lista membros |
| POST | `/lists/{id}/members` | OWNER | Adiciona membro por e-mail |
| PATCH | `/lists/{id}/members/{userId}` | OWNER | Muda papel |
| DELETE | `/lists/{id}/members/{userId}` | OWNER ou próprio | Remove / sai da lista |
| POST | `/lists/{id}/invites` | OWNER | Gera link de convite |
| GET | `/invites/{token}` | logado | Preview do convite |
| POST | `/invites/{token}/accept` | logado | Aceita convite (vira membro) |

## Frontend changes (PR 2)

A ser especificado no próximo PR. Itens previstos:
- Seletor de lista no header (lista ativa)
- Página de detalhes/membros da lista
- Botão "Convidar" com cópia de link e campo de e-mail
- Filtro `listId` integrado ao `api.tasks.list()`

## Compatibilidade

A API `/tasks` continua funcionando sem mudanças obrigatórias no frontend:
- Todo usuário tem uma lista padrão "Minhas Tarefas" criada no registro
- `POST /tasks` sem `listId` → usa a lista padrão
- `GET /tasks` sem `listId` → retorna tarefas de todas as listas acessíveis

## Out of scope

- UI de listas/membros/convites (PR 2)
- Categorias compartilhadas entre membros (categoria ainda é por usuário)
- Envio real de e-mail via SMTP
- Paginação de tarefas ou membros
- Notificações em tempo real

## Verification

```bash
cd apps/api
./mvnw test           # suíte completa verde
./mvnw spring-boot:run
```

Smoke manual (curl ou cliente HTTP, login via cookie):
1. `POST /auth/login {seed@todo.dev, seed123}` → cookie
2. `GET /lists` → lista "Minhas Tarefas" (OWNER)
3. `POST /lists {name: "Projeto X"}` → 201, virar OWNER
4. `POST /lists/{id}/members {email: lucas@todo.dev, role: EDITOR}` → 200
5. Login como lucas@todo.dev e `GET /lists` → ver "Projeto X" como EDITOR
6. `POST /lists/{id}/invites {role: VIEWER}` → token; `POST /invites/{token}/accept` → vira membro
7. `POST /tasks {title: "X", listId: id}` → tarefa na lista compartilhada
8. `GET /tasks` (sem listId) → inclui tarefas de todas as listas acessíveis
9. Frontend existente continua funcionando (login, CRUD de tarefas sem listId)
