# Configuração e desenvolvimento

[Voltar ao README](../README.md)

## Pré-requisitos

Docker e Docker Compose para executar a aplicação completa. JDK 25 para execução pelo Maven e testes; o Maven Wrapper está incluído.

## Arquivos e seleção de perfil

| Arquivo | Responsabilidade |
| --- | --- |
| `src/main/resources/application.properties` | Nome da aplicação, JPA, Flyway, erros, validade JWT e bootstrap do administrador |
| `src/main/resources/application-dev.properties` | Conexão local com padrões de desenvolvimento |
| `src/main/resources/application-prod.properties` | Conexão e segredo exigidos de produção |

O perfil padrão é `dev`. Ative `prod` explicitamente por `SPRING_PROFILES_ACTIVE=prod`, `--spring.profiles.active=prod` ao executar o JAR ou pela configuração da IDE. Não ative dev e prod simultaneamente.

Configurações comuns são herdadas pelos perfis: JPA valida o schema, Flyway executa migrations e o gerenciamento automático de Compose pelo Spring fica desabilitado. Os segredos são fornecidos pelo ambiente do processo ou pelo gerenciador de segredos da hospedagem; não são versionados nos arquivos de configuração.

## Conexão por perfil

| Propriedade Spring | Variável | Padrão em dev | Exigência em prod |
| --- | --- | --- | --- |
| `spring.datasource.url` | `DB_URL` | `jdbc:postgresql://localhost:5432/lindomar` | Obrigatória |
| `spring.datasource.username` | `DB_USER` | `lindomar` | Obrigatória |
| `spring.datasource.password` | `DB_PASSWORD` | `lindomar_dev`, exclusivamente local | Obrigatória |
| `app.jwt.secret` | `JWT_SECRET` | Sem segredo padrão válido | Obrigatória, mínimo 32 bytes UTF-8 |

Dev permite sobrescrever os padrões de conexão com as variáveis da tabela. Prod não possui fallback para as credenciais locais.

## JWT e administrador inicial

| Variável | Uso |
| --- | --- |
| `JWT_SECRET` | Segredo HS256 de pelo menos 32 bytes UTF-8 |
| `JWT_TTL_SECONDS` | Validade positiva do JWT em segundos; padrão 28800 (8 horas) |
| `ADMIN_NOME` | Nome inicial de até 120 caracteres |
| `ADMIN_EMAIL` | E-mail inicial válido de até 254 caracteres |
| `ADMIN_SENHA` | Senha inicial com mínimo de 8 caracteres e máximo de 72 bytes UTF-8 |

Configure as variáveis `ADMIN_*` apenas quando precisar criar o primeiro administrador de um banco novo. Não há conta administrativa fixa no repositório. Se já existir qualquer administrador, o bootstrap não cria outro nem redefine seus dados ou senha. Um e-mail já pertencente a um cliente não é promovido automaticamente.

Configure os valores no terminal ou nas opções de ambiente da IDE. Exemplo para gerar um segredo local:

```bash
export JWT_SECRET="$(openssl rand -hex 32)"
```

Segredo insuficiente, TTL não positivo ou configuração inicial incompleta impedem a inicialização. Não há recuperação de senha por endpoint. Cada banco mantém suas próprias contas; elas não são transferidas para a nuvem pelo Git ou pela imagem Docker.

## Docker em desenvolvimento

O `compose.yaml` seleciona `dev` e fornece conexão interna com PostgreSQL 17 Alpine. Banco e usuário são `lindomar`, com senha local `lindomar_dev`, coerentes com o perfil dev. Os valores locais não devem ser usados em produção.

Após definir JWT_SECRET e, se necessário, as variáveis ADMIN_* no processo:

```bash
docker compose up --build -d --wait
docker compose ps
```

O Compose aguarda o healthcheck do PostgreSQL e da API. A API responde em `http://localhost:8080`; Swagger UI em `http://localhost:8080/swagger-ui/index.html`.

Para alterar portas sem editar os arquivos:

```bash
POSTGRES_PORT=5433 API_PORT=8081 docker compose up --build -d --wait
```

A conexão interna do contêiner continua usando `postgres:5432`. O volume `postgres_data` preserva os dados. Alterar a configuração de inicialização do PostgreSQL não reconfigura um volume já existente.

## Maven em desenvolvimento

```bash
docker compose up -d postgres
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Se a porta do banco for alterada, ajuste DB_URL no processo Spring. SERVER_PORT altera a porta da execução direta; API_PORT altera apenas a publicação de porta no Compose. A IDE deve usar JDK 25 e o perfil dev.

## Produção

A hospedagem deve selecionar `SPRING_PROFILES_ACTIVE=prod` e fornecer DB_URL, DB_USER, DB_PASSWORD e JWT_SECRET. ADMIN_* são necessários somente para criar o administrador em um banco sem administrador. JWT_TTL_SECONDS é opcional.

Para executar a imagem com o Compose de produção:

```bash
docker compose -f compose.prod.yaml up --build -d --wait
```

Esse arquivo é independente do Compose de desenvolvimento: inicia apenas a API com perfil prod e usa PostgreSQL externo. Não combine os dois arquivos. A conexão obrigatória deve apontar para um banco acessível de dentro do contêiner. O comando falha se faltarem conexão ou segredo.

Para um JAR já construído:

```bash
java -jar target/bar-do-lindomar-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Conexão e segredos devem estar no ambiente desse processo. A mesma imagem contém os dois perfis; a seleção acontece na execução. O merge em main não executa deploy.

## Migrations e verificação

Migrations em `src/main/resources/db/migration`:

1. `V1__criar_usuarios_e_roles.sql`.
2. `V2__criar_categorias.sql`.
3. `V3__criar_produtos.sql`.

Nunca editar uma migration aplicada; adicionar uma nova. Flyway evolui o schema e JPA usa ddl-auto=validate. MapStruct gera mappers na compilação; DTOs usam records.

```bash
./mvnw clean verify
```

Os testes verificam a seleção dos perfis e usam PostgreSQL independente via Testcontainers para integração. Docker precisa estar disponível. O build da imagem não executa testes; rode a verificação pelo Maven antes da entrega.

Consulte [operação e publicação](operacao.md) para persistência, encerramento e integração Git.
