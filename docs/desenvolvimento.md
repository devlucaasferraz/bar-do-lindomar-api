# Ambiente de desenvolvimento

[Voltar ao README](../README.md)

## Pré-requisitos

JDK 25, Docker e Docker Compose. O Maven Wrapper está incluído.

## PostgreSQL local

Copie `.env.example` para `.env`, defina uma senha local e execute `docker compose up -d postgres`.
O Compose usa PostgreSQL 17 Alpine, volume persistente e healthcheck. Não apague o volume se quiser preservar os dados.
O arquivo `.env` é consumido pelo Compose; o Spring não o carrega automaticamente.

## Execução direta

Exporte `DB_URL` (padrão `jdbc:postgresql://localhost:5432/lindomar`), `DB_USER` (padrão `lindomar`) e `DB_PASSWORD`, correspondentes ao banco local.
Execute `./mvnw spring-boot:run`. A API usa a porta 8080. O gerenciamento automático do Compose está desabilitado para tornar a inicialização explícita.

## Migrations e build

Migrations ficam em `src/main/resources/db/migration` e seguem `V1__descricao.sql`, em sequência.
Nunca editar uma migration aplicada; criar uma nova. Flyway evolui o schema e JPA usa `ddl-auto=validate`.

`./mvnw clean verify` executa testes com PostgreSQL real via Testcontainers. Docker é obrigatório; o banco de testes é independente dos dados locais.

MapStruct gera mappers durante a compilação. DTOs novos usam records e não dependem de Lombok.
