# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Pointer operacional para Claude Code trabalhando neste repositório.

## Commands

```bash
# Backend (apps/api)
cd apps/api
./mvnw spring-boot:run                          # dev :8080
./mvnw clean package -DskipTests               # build JAR
./mvnw test                                     # todos os testes
./mvnw test -Dtest=TaskServiceTest              # teste específico (substitua o nome)

# Frontend (apps/web)
cd apps/web
npm run dev                                     # dev :3000
npm run build                                   # build produção
npm run lint                                    # lint (0 warnings tolerados)
```

## Architecture

Monorepo **poliglota** — Java e Node independentes.

```
apps/
  api/   # Spring Boot 3.5, Java 17, Maven — :8080
  web/   # Next.js 15, TypeScript, Tailwind, shadcn/ui — :3000
docs/    # documentação em 3 camadas (architecture / specs / adr)
```

### Backend (`apps/api`)

Fluxo de camadas: `Controller → Service → Repository (JPA) → Entity`

Package root: `com.springnexttodo`

| Pacote | Responsabilidade |
|--------|-----------------|
| `task/` | Feature principal: Entity, Repository, Service, Controller, DTOs |
| `config/` | CORS (`WebCorsConfig`), JPA Auditing (`JpaAuditingConfig`), seed data (`SeedData`) |
| `common/` | `ApiError` (modelo de erro) + `GlobalExceptionHandler` (@RestControllerAdvice) |

**Padrões importantes:**
- DTOs são Java Records imutáveis: `TaskRequest` (entrada validada) e `TaskResponse` (saída mapeada via `TaskResponse.from(entity)`).
- `TaskService` tem `@Transactional(readOnly = true)` na classe; métodos de escrita sobrescrevem com `@Transactional`.
- Timestamps (`createdAt`, `updatedAt`) são automáticos via `@CreatedDate`/`@LastModifiedDate` — nunca setar manualmente.
- Banco H2 in-memory com `ddl-auto: create-drop` — o schema é recriado a cada restart e os dados são perdidos. `SeedData` repopula usuário seed (`seed@todo.dev` / `seed123`) e 3 tarefas iniciais se o banco estiver vazio.
- H2 console desabilitado por segurança; para inspecionar o banco em dev use IntelliJ Database ou DBeaver com JDBC `jdbc:h2:mem:tododb`.
- JWT secret lido de `${JWT_SECRET}`; em dev usa fallback do `application.yml` — em produção, obrigatório definir a variável de ambiente.
- CORS libera apenas `http://localhost:3000`.
- `GlobalExceptionHandler` trata: `EntityNotFoundException` → 404, `MethodArgumentNotValidException` → 400 (com lista de campos), `Exception` genérico → 500.

**Endpoints REST:**

| Método | Path | Ação |
|--------|------|------|
| GET | `/tasks` | Lista todas, ordenadas por `createdAt` desc |
| GET | `/tasks/{id}` | Busca por ID |
| POST | `/tasks` | Cria (body: `{title, description}`) |
| PUT | `/tasks/{id}` | Atualiza título e descrição |
| PATCH | `/tasks/{id}/toggle` | Inverte `completed` |
| DELETE | `/tasks/{id}` | Remove (204 No Content) |

### Frontend (`apps/web`)

- App Router (`app/`), Server Components por padrão, Client Components apenas onde há interação.
- `lib/api.ts` é o único ponto de contato com a API — toda chamada HTTP passa por aqui.
- Dark theme forçado via classe `dark` no `<html>`.

## Git workflow

- **Branch protection:** `main` é protegida. **Nunca push direto em main.**
- Crie branch para cada mudança: `feat/nome`, `fix/nome`, `docs/nome`.
- Commits **atômicos** — um assunto por commit.
- Formato de mensagem: `<type>(<scope>): <description>` (ex: `feat(api): add toggle endpoint`).
- Abra PR e aguarde revisão antes de mergear.

## Documentation

Docs de referência em [docs/](docs/). Este arquivo é o pointer operacional — detalhes duráveis ficam em `docs/architecture/`.