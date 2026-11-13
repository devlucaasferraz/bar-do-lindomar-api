# Ambiente de desenvolvimento

[Voltar ao README](../README.md)

## Pré-requisitos

- Docker e Docker Compose para executar a aplicação completa.
- JDK 25 para executar pelo Maven e rodar testes. O Maven Wrapper está incluído.

## Configuração

Copie `.env.example` para `.env` e substitua os valores de senha e segredo. O arquivo `.env` é ignorado pelo Git e excluído do contexto de build Docker.

| Variável | Uso | Padrão |
| --- | --- | --- |
| `POSTGRES_DB` | Banco criado pelo Compose | `lindomar` |
| `POSTGRES_USER` | Usuário do banco no Compose | `lindomar` |
| `POSTGRES_PASSWORD` | Senha do banco no Compose | Obrigatória |
| `POSTGRES_PORT` | Porta do PostgreSQL no host | `5432` |
| `API_PORT` | Porta da API no host no Compose | `8080` |
| `JWT_SECRET` | Segredo HS256, mínimo 32 bytes UTF-8 | Obrigatório |
| `JWT_TTL_SECONDS` | Validade positiva do JWT em segundos | `28800` (8 horas) |
| `ADMIN_NOME` | Nome do administrador inicial, até 120 caracteres | Obrigatório na primeira execução |
| `ADMIN_EMAIL` | E-mail válido do administrador inicial, até 254 caracteres | Obrigatório na primeira execução |
| `ADMIN_SENHA` | Senha inicial, mínimo 8 caracteres e máximo 72 bytes UTF-8 | Obrigatória na primeira execução |

Gere um segredo aleatório, por exemplo com `openssl rand -hex 32`, e use o resultado como `JWT_SECRET`. Os valores de `.env.example` devem ser substituídos antes de executar.

## Execução completa com Docker

```bash
cp .env.example .env
# Edite .env com as credenciais locais e o segredo JWT.
docker compose up --build -d --wait
docker compose ps
```

A API fica em `http://localhost:8080` por padrão, e o Swagger em `http://localhost:8080/swagger-ui/index.html`.

O Compose inicia PostgreSQL 17 Alpine, aguarda seu healthcheck e inicia a API. A imagem da API também verifica sua prontidão consultando `/categorias`. O Dockerfile usa build multi-stage com Java 25; o contêiner final roda como usuário sem privilégios. O build da imagem não roda testes, pois os testes de integração precisam de acesso ao Docker; execute a verificação pelo Maven antes da entrega.

Para acompanhar e encerrar:

```bash
docker compose logs -f api
docker compose down
```

O volume `postgres_data` preserva os dados entre execuções. `docker compose down -v` apaga o banco; use apenas quando quiser descartar os dados locais. Alterar POSTGRES_USER/PASSWORD/DB não reconfigura um volume já inicializado.

## Execução direta com Maven

Suba somente o banco, usando o `.env` configurado:

```bash
docker compose up -d postgres
```

Exporte as variáveis do processo Spring no terminal ou configure-as na IDE:

- `DB_URL`: padrão `jdbc:postgresql://localhost:5432/lindomar`; ajuste banco e porta conforme o Compose.
- `DB_USER`: padrão `lindomar`.
- `DB_PASSWORD`: igual a `POSTGRES_PASSWORD`.
- `JWT_SECRET`, `JWT_TTL_SECONDS` e as variáveis `ADMIN_*` conforme a tabela.
- `SERVER_PORT`: porta HTTP na execução direta, padrão `8080`.

```bash
./mvnw spring-boot:run
```

O `.env` é consumido pelo Docker Compose; o Spring não o carrega automaticamente. `API_PORT` altera somente a publicação de porta no Compose. O gerenciamento automático de Compose pelo Spring está desabilitado.

## Administrador e login

Na primeira execução, a aplicação cria o administrador com hash BCrypt. E-mail é normalizado para minúsculas. Se já existir qualquer administrador, o bootstrap não cria outro nem altera senha ou dados existentes. Se não houver administrador e o e-mail escolhido pertencer a um cliente, a aplicação falha sem promover esse usuário.

Falta de configuração inicial, dados inválidos, segredo JWT insuficiente ou TTL não positivo impedem a inicialização. Não há recuperação de senha por endpoint nesta versão. Alterar `ADMIN_SENHA` depois do primeiro cadastro não troca a senha persistida.

O bootstrap usa o banco conectado à aplicação. Um banco novo na nuvem precisa de suas próprias variáveis `ADMIN_*`; usuários locais não são transferidos automaticamente. Não existe conta administrativa fixa nas migrations, no código ou na imagem Docker.

## Migrations e verificação

Migrations ficam em `src/main/resources/db/migration`, com sequência única:

1. `V1__criar_usuarios_e_roles.sql`.
2. `V2__criar_categorias.sql`.
3. `V3__criar_produtos.sql`.

Nunca editar uma migration aplicada; adicionar uma nova. Flyway evolui o schema e JPA usa `ddl-auto=validate`. MapStruct gera mappers na compilação; DTOs usam records.

```bash
./mvnw clean verify
```

Os testes usam PostgreSQL real via Testcontainers, com banco independente dos dados locais. Docker é obrigatório e precisa conseguir obter as imagens de teste. As credenciais de teste são exclusivas desse ambiente.

## Produção

A branch `main` recebe o código validado em `develop`. O merge é local enquanto não houver remoto configurado e não publica a aplicação na nuvem. Hospedagem e HTTPS continuam dependendo da implantação. Consulte [operação e publicação](operacao.md). Tokens existentes continuam válidos até expirar; não há refresh token ou revogação individual nesta versão. Trocar o segredo invalida todos os tokens anteriores. Restrinja acesso ao banco e injete credenciais pelo ambiente da hospedagem.
