# AGENTS.md

Pointer operacional para agentes de IA (OpenAI Codex, etc.) trabalhando neste repositório.
Para Claude Code, veja [CLAUDE.md](CLAUDE.md) — o conteúdo é idêntico.

## Commands

```bash
# Backend
cd apps/api && ./mvnw spring-boot:run       # dev :8080
cd apps/api && ./mvnw clean package -DskipTests

# Frontend
cd apps/web && npm run dev                  # dev :3000
cd apps/web && npm run build && npm run lint
```

## Architecture

```
apps/api   → Spring Boot 3.5 + JPA + H2 (Java 17, Maven)
apps/web   → Next.js 16 + Tailwind + shadcn/ui (TypeScript)
docs/      → architecture (evergreen) / specs (por PR) / adr (decisões)
```

## Git rules

- `main` é protegida. Nunca push direto.
- Branch por feature/fix, PR obrigatório, commits atômicos.
- Formato: `feat|fix|docs|refactor|test(scope): description`