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

## Administrador inicial e JWT

Exporte `JWT_SECRET` com pelo menos 32 bytes aleatórios (por exemplo, gerados com `openssl rand -hex 32`). Não reutilize o valor do exemplo em produção. `JWT_TTL_SECONDS` é positivo e tem padrão 28800.

Na primeira execução, informe `ADMIN_NOME`, `ADMIN_EMAIL` e `ADMIN_SENHA`. A senha precisa de pelo menos 8 caracteres e no máximo 72 bytes UTF-8; nome tem até 120 caracteres e e-mail válido até 254. Se já existir administrador, essas variáveis não alteram sua senha. Se o e-mail informado pertencer a um cliente e não existir administrador, a aplicação falha sem promover esse usuário.

A aplicação falha na inicialização se o segredo JWT for insuficiente ou faltar configuração para criar o primeiro administrador. Não existe recuperação de senha por endpoint nesta versão; proteja as credenciais de bootstrap.
