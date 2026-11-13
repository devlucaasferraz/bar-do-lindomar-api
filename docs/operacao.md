# Operação e publicação

[Voltar ao README](../README.md)

## Iniciar e conferir a aplicação

Configure o ambiente conforme o [guia de desenvolvimento](desenvolvimento.md). O `.env` local é usado pelo Compose, fica fora do Git e não faz parte da imagem.

```bash
docker compose up --build -d --wait
docker compose ps
```

O comando aguarda os healthchecks do PostgreSQL e da API. A API verifica prontidão por `/categorias`. Os endereços padrão são:

- API: `http://localhost:8080`.
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`.
- OpenAPI: `http://localhost:8080/v3/api-docs`.

Se a porta estiver ocupada, ajuste `API_PORT` ou `POSTGRES_PORT` no `.env`. A conexão entre a API e o banco dentro do Compose continua usando a porta interna 5432 do serviço `postgres`.

## Administrador por ambiente

Não há conta administrativa fixa no repositório. Na primeira inicialização de um banco sem administrador, defina `ADMIN_NOME`, `ADMIN_EMAIL` e `ADMIN_SENHA`. O bootstrap cria a conta com senha armazenada como hash BCrypt. As roles ADMIN e CLIENTE são inseridas por migration, sem credenciais.

Se já existir administrador, o bootstrap não cria outra conta nem muda a senha existente. Alterar variáveis `ADMIN_*` não redefine a conta persistida. Não há recuperação de senha ou gerenciamento de usuários por endpoint nesta versão.

Uma conta criada localmente permanece no banco local. Ela não é apagada nem enviada à nuvem por um merge ou deploy. Uma instalação na nuvem com banco separado cria seu próprio administrador. Somente uma migração ou restauração explícita dos dados levaria os usuários locais para outro banco.

Para entrar, use `POST /auth/login` com as credenciais daquele ambiente. No Swagger, copie o campo `token` da resposta e informe o JWT no botão **Authorize**. O contrato completo está em [API](api.md).

## Persistência e encerramento

```bash
docker compose logs -f api
docker compose down
```

Encerrar os contêineres mantém o volume `postgres_data`. Ao iniciar novamente com o mesmo projeto Compose, contas e produtos permanecem. `docker compose down -v` remove o volume e seus dados; a próxima inicialização executa migrations e bootstrap em um banco vazio.

Mudar usuário, senha ou nome do banco nas variáveis POSTGRES_* não altera um volume já inicializado. Não use remoção de volume para trocar credenciais de um banco com dados que precisam ser preservados.

## Integração e publicação

1. Desenvolver em `feature/nome-da-feature`, criada a partir de `develop`.
2. Validar a feature, atualizar documentos e Swagger e criar um commit completo.
3. Integrar a feature em `develop`.
4. Executar `./mvnw clean verify` com JDK 25 e Docker disponíveis antes da publicação.
5. Integrar `develop` em `main` por fast-forward quando não houver divergências.

Sem remoto configurado, commits e merges ficam apenas no repositório local. O merge em `main` não provisiona serviços, não faz push e não publica a API.

Na futura hospedagem, configurar PostgreSQL persistente, conexão `DB_URL`/`DB_USER`/`DB_PASSWORD`, segredo `JWT_SECRET`, credenciais iniciais `ADMIN_*` e HTTPS. Manter os mesmos valores de conexão após reinícios e preservar o banco entre versões. Flyway aplica apenas migrations ainda não executadas; JPA valida o schema.

Tokens expiram em 8 horas por padrão, sem refresh token ou revogação individual. Trocar o segredo JWT invalida os tokens anteriores. Credenciais e segredos devem ficar no ambiente da hospedagem, fora do código e da documentação.
