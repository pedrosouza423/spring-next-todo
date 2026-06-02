# spring-next-todo

Monorepo de estudo colaborativo: To-Do app full-stack com Spring Boot (Java) no backend e Next.js no frontend.

**Devs:** Pedro (backend/full-stack) + Lucas (frontend).

## Apps

| App | Descrição | Porta |
| --- | --- | --- |
| `apps/api` | REST API — Spring Boot 3.5 + Spring Data JPA + H2 | 8080 |
| `apps/web` | Frontend — Next.js 16 + Tailwind + shadcn/ui | 3000 |

## Stack

- **Backend:** Java 17, Spring Boot 3.5.3, Spring Web, Spring Data JPA, H2 (em memória), Bean Validation, Maven
- **Frontend:** Next.js 16 (App Router), TypeScript, Tailwind CSS, shadcn/ui, Lucide Icons
- **Monorepo:** apps independentes (Java + Node), docs compartilhado na raiz

## Getting Started

### 1. Clonar

```bash
git clone https://github.com/pedrosouza423/spring-next-todo.git
cd spring-next-todo
```

### 2. Rodar o backend

```bash
cd apps/api
./mvnw spring-boot:run
```

API disponível em `http://localhost:8080`.  
Console H2 em `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:tododb`, user: `sa`, senha vazia).

### 3. Rodar o frontend

```bash
cd apps/web
npm install
npm run dev
```

App disponível em `http://localhost:3000`.

> O `.env.local` já vem pré-configurado apontando para `http://localhost:8080`.

## Comandos comuns

```bash
# Backend
cd apps/api
./mvnw clean package          # build (gera JAR)
./mvnw clean package -DskipTests  # build sem testes
./mvnw spring-boot:run        # run em dev
./mvnw test                   # testes

# Frontend
cd apps/web
npm run dev                   # dev server :3000
npm run build                 # build de produção
npm run lint                  # lint
```

## Endpoints da API

| Método | Rota | Descrição |
| --- | --- | --- |
| `GET` | `/tasks` | Lista todas as tarefas |
| `GET` | `/tasks/{id}` | Busca tarefa por ID |
| `POST` | `/tasks` | Cria nova tarefa |
| `PUT` | `/tasks/{id}` | Atualiza título e descrição |
| `PATCH` | `/tasks/{id}/toggle` | Alterna concluída/pendente |
| `DELETE` | `/tasks/{id}` | Remove tarefa |

## Branch workflow

- `main` é protegida — **nunca faça push direto**.
- Crie uma branch para cada feature/fix: `feat/nome`, `fix/nome`.
- Abra PR para `main`; pelo menos 1 aprovação antes do merge.
- Commits atômicos e descritivos (ex: `feat(api): add toggle endpoint`).

## Documentação

Em `docs/` — arquitetura, domain model, rotas da API e decisões técnicas.  
Comece por [docs/README.md](docs/README.md).