# Ambiente de desenvolvimento

[Voltar ao README](../README.md)

## Ambiente local

### Pré-requisitos

- JDK 25 e `JAVA_HOME` configurado.
- Docker com Docker Compose disponível.

O Maven Wrapper está incluído; não é necessário instalar Maven separadamente.

### Executar a aplicação

Na raiz do repositório:

```bash
./mvnw spring-boot:run
```

A dependência de integração com Docker Compose está presente para gerenciar o serviço definido em `compose.yaml` durante o desenvolvimento. O Compose atual disponibiliza PostgreSQL com estes valores locais:

| Configuração | Valor atual |
| --- | --- |
| Banco | `mydatabase` |
| Usuário | `myuser` |
| Senha | `secret` |
| Porta interna do PostgreSQL | `5432` |
| Porta no host | Atribuída dinamicamente pelo Docker |

Essas credenciais são apenas para desenvolvimento. A porta do host pode ser consultada com:

```bash
docker compose port postgres 5432
```

Para gerenciar o banco manualmente:

```bash
docker compose up -d postgres
docker compose ps
docker compose down
```

O Compose usa `postgres:latest` e ainda não declara um volume de dados. Antes de depender desse ambiente para dados persistentes, definir uma versão fixa da imagem e um volume. Ainda não existe Dockerfile para executar a própria API em um contêiner.

A porta HTTP padrão esperada é `8080`. Com a aplicação iniciada e a integração Springdoc disponível, a interface da documentação fica em `http://localhost:8080/swagger-ui/index.html`, e o documento OpenAPI em `http://localhost:8080/v3/api-docs`. Os endpoints de negócio aparecerão conforme forem implementados.

### Build e testes

```bash
./mvnw test
./mvnw clean verify
```

O teste de contexto existente pode depender do Docker e do PostgreSQL, pois ainda não há um perfil de testes isolado. As features devem incluir testes de suas regras, validações e permissões, especialmente a rejeição de operações de escrita por clientes.

## Banco de dados e migrations

As migrations devem ficar em `src/main/resources/db/migration`, com nomes como:

```text
V1__criar_categorias.sql
V2__criar_produtos.sql
V3__criar_usuarios_e_roles.sql
```

Os nomes são exemplos; a numeração deve seguir a sequência real do repositório. Uma migration já aplicada não deve ser editada: mudanças posteriores exigem uma nova migration. O Flyway deve ser responsável pela evolução do schema; a configuração JPA prevista é `ddl-auto=validate`.

Dados de referência, como categorias e roles, devem ser inseridos por migrations. Credenciais de administradores não devem ser versionadas em scripts SQL. A configuração de produção deve receber conexão e credenciais por variáveis de ambiente ou mecanismo equivalente, fora do código.
