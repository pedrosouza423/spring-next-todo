# Design — Categorias nas tarefas (Issue #2)

**Status:** Approved  
**Branch:** `feat/categories`  
**Date:** 2026-06-05  
**Approach:** ManyToOne — uma categoria por tarefa, CRUD completo, isolamento por usuário

---

## Contexto

Issue #2 pede a capacidade de organizar tarefas por categoria (ex: Trabalho, Estudo, Pessoal). Auth já está implementada (issue #1), então categorias são isoladas por usuário — cada usuário gerencia as suas próprias.

Relação escolhida: `ManyToOne` (task pertence a no máximo uma categoria). ManyToMany foi descartado por complexidade desnecessária neste escopo; "tags múltiplas" ficará para avaliação futura.

---

## Modelo de dados

### Nova entidade `Category`

| Campo  | Tipo   | Restrições |
|--------|--------|------------|
| id     | Long   | PK, auto-increment |
| name   | String | NOT NULL, max 50 chars |
| color  | String | NOT NULL, 7 chars, formato `#RRGGBB` |
| user   | User   | @ManyToOne, FK `user_id`, NOT NULL |

### Modificação em `Task`

```
category   Category   @ManyToOne(optional=true, fetch=LAZY), FK category_id nullable
```

FK nullable: tasks existentes e novas sem categoria ficam com `category = null`. A relação é `LAZY` para não carregar categoria em queries que não precisam dela.

**Deleção de categoria:** dentro de uma única transação, o serviço nulifica `category` em todas as tasks afetadas antes de deletar a categoria — sem FK violation, sem erro para o usuário.

**Scope:** categorias são isoladas por usuário. Toda query filtra por `user`. User A e User B podem ter categorias com o mesmo nome sem conflito.

---

## API — novo recurso `/categories`

Todos os endpoints requerem autenticação (cookie `auth_token`).

| Método | Path | Body | Status | Resposta |
|--------|------|------|--------|----------|
| GET | `/categories` | — | 200 | `CategoryResponse[]` ordenado por `name` |
| POST | `/categories` | `CategoryRequest` | 201 | `CategoryResponse` |
| PUT | `/categories/{id}` | `CategoryRequest` | 200 | `CategoryResponse` |
| DELETE | `/categories/{id}` | — | 204 | — |

**`CategoryRequest`** (record):
- `name`: `@NotBlank`, `@Size(max=50)`
- `color`: `@NotBlank`, `@Pattern(regexp="^#[0-9A-Fa-f]{6}$")`

**`CategoryResponse`** (record): `{ id, name, color }`

Erros: `id` inexistente ou de outro usuário → `EntityNotFoundException` → 404 (já tratado pelo `GlobalExceptionHandler`).

## API — modificações em `/tasks`

**`TaskResponse`** ganha campo:
```json
"category": { "id": 2, "name": "Estudo", "color": "#3b82f6" }
```
Nullable — `null` quando a task não tem categoria.

**`TaskRequest`** ganha campo:
```json
"categoryId": 2
```
Opcional (nullable). Se informado, o serviço valida que a categoria existe e pertence ao usuário autenticado (404 caso contrário).

**Filtro:**
```
GET /tasks?categoryId=2   → tasks da categoria 2 do usuário autenticado
GET /tasks                → todas as tasks (comportamento atual preservado)
```

---

## Estrutura backend

### Novo pacote `com.springnexttodo.category`

```
category/
  Category.java
  CategoryRepository.java
  CategoryService.java
  CategoryController.java
  dto/
    CategoryRequest.java
    CategoryResponse.java
```

**`CategoryRepository`:** `findByUserOrderByName(User)`, `findByIdAndUser(Long, User)`

**`CategoryService`:** `@Transactional(readOnly=true)` na classe; métodos de escrita sobrescrevem. `delete` nulifica tasks antes de deletar a categoria.

**`CategoryController`:** injeta `CategoryService` e `AuthService`; resolve `currentUser` via `Authentication` (mesmo padrão do `TaskController`).

### Arquivos modificados

| Arquivo | Mudança |
|---------|---------|
| `task/Task.java` | `+@ManyToOne(optional=true, fetch=LAZY) Category category` |
| `task/dto/TaskRequest.java` | `+Long categoryId` (nullable) |
| `task/dto/TaskResponse.java` | `+CategoryResponse category` (nullable); `from(entity)` atualizado |
| `task/TaskRepository.java` | `+findByUserAndCategoryOrderByCreatedAtDesc(User, Category)` |
| `task/TaskService.java` | `findAll` aceita `Optional<Long> categoryId`; `create`/`update` resolvem categoria via `CategoryService` |
| `task/TaskController.java` | `list()` aceita `@RequestParam(required=false) Long categoryId`; injeta `CategoryService` |
| `config/SeedData.java` | Cria 3 categorias (Trabalho `#3b82f6`, Estudo `#10b981`, Pessoal `#f59e0b`) e associa às tasks seed |

---

## Frontend

### Novos tipos em `api.ts`

```ts
interface Category { id: number; name: string; color: string }
// Task ganha: category: Category | null
// create/update ganham: categoryId?: number | null
// api.categories: list / create / update / delete
```

### Novos componentes

| Componente | Tipo | Propósito |
|------------|------|-----------|
| `CategoryBadge.tsx` | Client | Badge colorido (`Badge` do shadcn); recebe `category: Category` |
| `CategorySelector.tsx` | Client | `<select>` com swatch de cor + opção "Sem categoria"; recebe `categories: Category[]` + `value` + `onChange` |

### Componentes modificados

| Arquivo | Mudança |
|---------|---------|
| `TaskItem.tsx` | Mostra `<CategoryBadge>` abaixo do título quando `task.category != null` |
| `TaskForm.tsx` | `<CategorySelector>` expande junto com o campo de descrição; inclui `categoryId` no payload |
| `TaskEditDialog.tsx` | `<CategorySelector>` no formulário de edição |
| `page.tsx` | Carrega categorias uma vez (`api.categories.list()`); passa como prop; botões de filtro por categoria no topo da lista |

---

## Testes

- `CategoryServiceTest` — `findAll` retorna só categorias do usuário; `create`; `update`; `delete` (nulifica tasks); acesso de outro usuário → 404
- `TaskServiceTest` — `findAll` com `categoryId` válido; `findAll` com `categoryId` de outro usuário → lista vazia ou 404; `create` com `categoryId` válido; `create` com `categoryId` inválido → EntityNotFoundException

---

## Seed de desenvolvimento

A cada restart (banco vazio):
- Categorias do seed user (`seed@todo.dev`):
  - `Trabalho` — `#3b82f6` (azul)
  - `Estudo` — `#10b981` (verde)
  - `Pessoal` — `#f59e0b` (âmbar)
- Tasks seed associadas: t1 → Estudo, t2 → Trabalho, t3 → sem categoria
