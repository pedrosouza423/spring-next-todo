# spring-next-todo — Frontend

Next.js 15 frontend para o projeto [spring-next-todo](../../README.md).

## Como usar

### 1. Subir o backend primeiro

```bash
cd apps/api
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Na primeira inicialização, cria automaticamente:
- **Usuário seed:** `seed@todo.dev` / `seed123`
- 3 tarefas de exemplo

### 2. Subir o frontend

```bash
cd apps/web
npm install      # só na primeira vez
npm run dev
```

Acesse `http://localhost:3000`.

## Fluxo de autenticação

| Rota | Descrição |
|------|-----------|
| `/login` | Entrar com email + senha |
| `/register` | Criar conta nova |
| `/` | Lista de tarefas (protegida — redireciona para `/login` se não autenticado) |

- O token JWT fica em um **httpOnly cookie** — nunca exposto ao JavaScript.
- Cada usuário vê apenas as suas próprias tarefas.
- **Sair:** botão no cabeçalho da página principal.

## Conta de teste rápido

```
Email: seed@todo.dev
Senha: seed123
```

> Os dados são perdidos quando o backend reinicia (H2 in-memory).

## Comandos

```bash
npm run dev      # dev server em :3000
npm run build    # build de produção
npm run lint     # lint (0 warnings tolerados)
```
