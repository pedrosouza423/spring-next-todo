# CLAUDE.md

Pointer operacional para Claude Code trabalhando neste repositório.

## Commands

```bash
# Backend (apps/api)
cd apps/api
./mvnw spring-boot:run          # dev :8080
./mvnw clean package -DskipTests # build JAR
./mvnw test                      # testes

# Frontend (apps/web)
cd apps/web
npm run dev                      # dev :3000
npm run build                    # build produção
npm run lint                     # lint (0 warnings)
```

## Architecture

Monorepo **poliglota** — Java e Node independentes.

```
apps/
  api/   # Spring Boot 3.5, Java 17, Maven — :8080
  web/   # Next.js 16, TypeScript, Tailwind, shadcn/ui — :3000
docs/    # documentação em 3 camadas (architecture / specs / adr)
```

### Backend layers

`Controller → Service → Repository (JPA) → Entity`

Package root: `com.springnexttodo`  
Sub-pacotes: `task/`, `config/`, `common/`

### Frontend

- App Router (`app/`), Server Components por padrão, Client Components apenas onde há interação.
- `lib/api.ts` é o único ponto de contato com a API.
- Dark theme forçado via classe `dark` no `<html>`.

## Git workflow

- **Branch protection:** `main` é protegida. **Nunca push direto em main.**
- Crie branch para cada mudança: `feat/nome`, `fix/nome`, `docs/nome`.
- Commits **atômicos** — um assunto por commit.
- Formato de mensagem: `<type>(<scope>): <description>` (ex: `feat(api): add toggle endpoint`).
- Abra PR e aguarde revisão antes de mergear.

## Documentation

Docs de referência em [docs/](docs/). Este arquivo é o pointer operacional — detalhes duráveis ficam em `docs/architecture/`.