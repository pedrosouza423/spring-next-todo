# Spec — Auth de usuários (Issue #1)

**Status:** Implemented  
**Branch:** `feat/auth-jwt`  
**Date:** 2026-06-04

## Contexto

O MVP não tinha autenticação — todas as tasks eram globais. Esta spec introduz
Spring Security + JWT para que cada usuário tenha sua própria lista isolada.

## Decisões de design

| Decisão | Escolha | Justificativa |
|---------|---------|---------------|
| Token storage | httpOnly cookie (`auth_token`) | Imune a XSS; browser envia automaticamente |
| Biblioteca JWT | JJWT 0.12.x | API moderna e didática para quem está aprendendo |
| Token único | Access token 24h, sem refresh | Suficiente para estudo; sem complexidade de renovação |
| Banco de dados | H2 in-memory (mantido) | Escopo isolado; migração para Postgres fica na issue #7 |

## Arquitetura

### Backend — novos arquivos em `com.springnexttodo.auth`

```
auth/
  User.java                  — @Entity tabela users
  UserRepository.java        — findByEmail, existsByEmail
  JwtService.java            — generateToken, extractEmail, isValid (JJWT 0.12)
  UserDetailsServiceImpl.java — bridge Spring Security ↔ User
  JwtAuthFilter.java         — OncePerRequestFilter, lê cookie auth_token
  AuthService.java           — register, findByEmail, getUser
  AuthController.java        — POST /auth/register|login, GET /auth/me, POST /auth/logout
  EmailAlreadyUsedException.java
  dto/
    RegisterRequest.java     — name, email, password
    LoginRequest.java        — email, password
    AuthResponse.java        — id, name, email (sem token — vai só no cookie)
```

### Backend — arquivos modificados

| Arquivo | Mudança |
|---------|---------|
| `config/SecurityConfig.java` | Novo bean: FilterChain stateless, permits /auth/** |
| `task/Task.java` | Campo `@ManyToOne User user` (user_id FK) |
| `task/TaskRepository.java` | `findByUserOrderByCreatedAtDesc`, `findByIdAndUser` |
| `task/TaskService.java` | Todos os métodos recebem `User` autenticado |
| `task/TaskController.java` | Injeta `AuthService`, resolve user via `Authentication` |
| `config/SeedData.java` | Cria seed user + associa as 3 tasks |
| `config/WebCorsConfig.java` | `allowCredentials(true)` |
| `common/GlobalExceptionHandler.java` | Handlers: 409 EmailAlreadyUsed, 401 BadCredentials |

### Frontend — novos arquivos

```
src/middleware.ts               — gate: redireciona sem cookie → /login
src/components/auth/
  LoginForm.tsx                 — formulário email + senha
  RegisterForm.tsx              — formulário name + email + senha
  LogoutButton.tsx              — Client Component, chama api.auth.logout()
src/app/login/page.tsx
src/app/register/page.tsx
```

`src/lib/api.ts` — adicionado `credentials: "include"` + namespace `api.auth`.

## Endpoints

| Método | Path | Auth | Ação |
|--------|------|------|------|
| POST | `/auth/register` | público | Cria user → 201 |
| POST | `/auth/login` | público | Valida credenciais → seta cookie → 200 |
| GET | `/auth/me` | autenticado | Retorna user atual |
| POST | `/auth/logout` | autenticado | Limpa cookie → 204 |

## Seed de desenvolvimento

A cada restart, se o banco estiver vazio:
- User: `seed@todo.dev` / senha `seed123`
- 3 tasks de exemplo associadas a este user

## Testes

- `JwtServiceTest` — geração, roundtrip, expirado, adulterado, null/blank
- `AuthServiceTest` — register (sucesso + email duplicado), findByEmail (achado + não encontrado)
- `TaskServiceTest` — findAll, findById, acesso de outro user (404), create com user, delete de outro user
