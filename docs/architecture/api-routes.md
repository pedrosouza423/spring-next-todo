# Architecture — API Routes

**Last updated:** 2026-06-02

Base URL: `http://localhost:8080`  
Content-Type: `application/json`

## Endpoints

### `GET /tasks`

Lista todas as tarefas, ordenadas por `createdAt` decrescente.

**Response 200**
```json
[
  {
    "id": 1,
    "title": "Estudar Spring Boot",
    "description": "Entender camadas Controller → Service → Repository",
    "completed": false,
    "createdAt": "2026-06-02T10:00:00Z",
    "updatedAt": "2026-06-02T10:00:00Z"
  }
]
```

---

### `GET /tasks/{id}`

**Response 200** — objeto `Task` (mesmo schema acima).  
**Response 404** — task não encontrada.

---

### `POST /tasks`

**Request body**
```json
{ "title": "Minha tarefa", "description": "opcional" }
```

`title` é obrigatório (max 255 chars). `description` é opcional (max 2000 chars).

**Response 201** — objeto `Task` criado.  
**Response 400** — falha de validação (ver Error Envelope abaixo).

---

### `PUT /tasks/{id}`

Atualiza título e descrição. Mesmo schema de request que `POST`.

**Response 200** — objeto `Task` atualizado.  
**Response 404** — task não encontrada.

---

### `PATCH /tasks/{id}/toggle`

Alterna o campo `completed` (true → false, false → true).

**Response 200** — objeto `Task` com `completed` atualizado.  
**Response 404** — task não encontrada.

---

### `DELETE /tasks/{id}`

**Response 204** — sem body.  
**Response 404** — task não encontrada.

---

## Error Envelope

Todos os erros retornam o mesmo formato:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": ["title: title is required"],
  "timestamp": "2026-06-02T10:00:00Z"
}
```

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `status` | int | HTTP status code |
| `message` | string | Mensagem de alto nível |
| `errors` | string[] | Lista de erros detalhados (vazia em 404/500) |
| `timestamp` | ISO-8601 | Instante do erro |