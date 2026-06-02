# ADR 0001 — H2 em memória no MVP

**Status:** Accepted  
**Date:** 2026-06-02

## Context

O projeto de estudo envolve dois devs com ambientes distintos. Docker é uma dependência extra que aumenta a fricção no setup inicial. Como o foco do MVP é aprender as camadas Spring (Controller/Service/Repository/Entity) e a integração com o frontend, um banco real não é requisito.

## Decision

Usar **H2 em memória** (`jdbc:h2:mem:tododb`) no MVP. A configuração fica inteiramente em `application.yml` — nenhum serviço externo necessário.

## Consequences

- **Setup em 1 comando:** `./mvnw spring-boot:run` — sem Docker, sem instalação de banco.
- **Trade-off:** dados são voláteis — perdidos ao reiniciar. Aceitável em fase de estudo.
- **Migração planejada:** issue #6 cobre a troca para PostgreSQL via `docker-compose.yml`. Quando feita, só `application.yml` e `pom.xml` mudam (nenhuma lógica de negócio).