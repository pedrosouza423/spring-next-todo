# Architecture — Domain Model

**Last updated:** 2026-06-02

## Entidade `Task`

```
tasks
┌──────────────┬─────────────────┬───────────┐
│ Column       │ Type            │ Notes     │
├──────────────┼─────────────────┼───────────┤
│ id           │ BIGINT (PK, AI) │           │
│ title        │ VARCHAR(255)    │ NOT NULL  │
│ description  │ VARCHAR(2000)   │ nullable  │
│ completed    │ BOOLEAN         │ default F │
│ created_at   │ TIMESTAMP       │ auto, R/O │
│ updated_at   │ TIMESTAMP       │ auto      │
└──────────────┴─────────────────┴───────────┘
```

`created_at` e `updated_at` são gerenciados pelo **Spring Data JPA Auditing** — nunca escritos manualmente.

## DTOs

| DTO | Usado em | Campos |
| --- | --- | --- |
| `TaskRequest` | `POST /tasks`, `PUT /tasks/{id}` | `title` (obrigatório), `description` (opcional) |
| `TaskResponse` | todas as respostas | todos os campos da entidade |

## Evolução futura (backlog)

Com a feature de **usuários** (issue #1), `Task` ganhará `user_id FK` e `tasks` terá um índice composto `(user_id, created_at)`.  
Com **categorias** (issue #2), haverá uma tabela `categories` e uma relação `ManyToMany`.