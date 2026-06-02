# Architecture — Local Development

**Last updated:** 2026-06-02

## Pré-requisitos

| Ferramenta | Versão mínima | Nota |
| --- | --- | --- |
| Java (JDK) | 17 | `JAVA_HOME` deve apontar para JDK 17 |
| Maven | — | Use o wrapper `./mvnw` — não precisa instalar |
| Node.js | 18+ | Testado com Node 24 |
| npm | 9+ | Incluído com Node |
| Git | — | |

## Setup

```bash
git clone https://github.com/pedrosouza423/spring-next-todo.git
cd spring-next-todo
```

## Rodando o backend

```bash
cd apps/api
./mvnw spring-boot:run
```

- API em: `http://localhost:8080`
- Console H2: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:tododb`
  - User: `sa` | Senha: *(vazia)*
- Seed automático: 3 tarefas de exemplo são criadas na primeira inicialização.
- **Os dados são perdidos ao reiniciar** (in-memory H2).

## Rodando o frontend

```bash
cd apps/web
npm install      # só na primeira vez
npm run dev
```

- App em: `http://localhost:3000`
- O arquivo `.env.local` já aponta para `http://localhost:8080`. Não precisa configurar.

## Ambos rodando

Abra dois terminais:

```
terminal 1 → cd apps/api  && ./mvnw spring-boot:run
terminal 2 → cd apps/web  && npm run dev
```

Acesse `http://localhost:3000`.

## Comandos de build

```bash
# backend
cd apps/api
./mvnw clean package          # gera target/api-0.0.1-SNAPSHOT.jar
./mvnw clean package -DskipTests

# frontend
cd apps/web
npm run build   # gera .next/
npm run lint    # lint (0 warnings)
```

## Portas

| Serviço | Porta |
| --- | --- |
| Spring Boot API | 8080 |
| H2 Console | 8080/h2-console |
| Next.js dev | 3000 |