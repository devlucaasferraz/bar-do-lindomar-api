<h1 align="center" style="font-weight: bold;">Bar do Lindomar API 🍻</h1>

<p align="center">
  <a href="#technologies">Tecnologias</a> •
  <a href="#started">Como executar</a> •
  <a href="#routes">API Endpoints</a> •
  <a href="#colab">Colaboradores</a> •
  <a href="#contribute">Contribuir</a>
</p>

<p align="center">
  <b>API de um aplicativo mobile simples para consultar e gerenciar os produtos do Bar do Lindomar.</b>
</p>

O administrador adiciona, modifica e remove produtos. Clientes consultam o catálogo, pesquisam produtos e filtram por categoria.

Catálogo público com pesquisa e paginação. A manutenção de produtos exige login de administrador por JWT.

<h2 id="technologies">💻 Tecnologias</h2>

- Java 25 e Maven Wrapper.
- Spring Boot 4.1.2, Spring MVC e Spring Data JPA.
- Jakarta Bean Validation.
- PostgreSQL e Flyway.
- Docker e Docker Compose.
- Springdoc OpenAPI e Swagger UI.
- MapStruct para entidades e DTOs.
- Spring Security e JWT.
- Testcontainers para testes com PostgreSQL real.

A arquitetura é MVC. Veja os detalhes de [arquitetura e domínio](docs/arquitetura.md) e as [regras de negócio](docs/regras-de-negocio.md).

<h2 id="started">🚀 Como executar</h2>

<h3>Pré-requisitos</h3>

- Docker com Docker Compose para executar a aplicação completa.
- Git para clonar e contribuir.
- JDK 25 com `JAVA_HOME` configurado somente para executar pelo Maven ou rodar os testes.

<h3>Clonando</h3>

O repositório ainda não possui remoto configurado. Substitua `<URL_DO_REPOSITORIO>` pela URL quando estiver disponível:

```bash
git clone <URL_DO_REPOSITORIO>
cd bar-do-lindomar-api
```

<h3>Perfis de configuração</h3>

- `application.properties`: configurações comuns e perfil padrão `dev`.
- `application-dev.properties`: conexão local de desenvolvimento.
- `application-prod.properties`: conexão e segredo obrigatórios do ambiente de produção.

O perfil `dev` usa PostgreSQL local com banco/usuário `lindomar` e senha exclusivamente local `lindomar_dev`. Configure `JWT_SECRET` no processo e, para um banco sem administrador, informe `ADMIN_NOME`, `ADMIN_EMAIL` e `ADMIN_SENHA`. Não há conta administrativa fixa no código.

```bash
export JWT_SECRET="$(openssl rand -hex 32)"
```

Configure as credenciais iniciais no terminal ou na IDE antes de iniciar um banco novo. Elas não devem ser versionadas. Veja os detalhes e comandos em [configuração por perfis](docs/desenvolvimento.md).

<h3>Iniciando</h3>

Desenvolvimento, com API e banco locais:

```bash
docker compose up --build -d --wait
```

API em `http://localhost:8080`, com PostgreSQL persistido em volume. Para executar somente a API pelo Maven, suba o banco com `docker compose up -d postgres` e use:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Para produção, selecione explicitamente o perfil `prod` e forneça a conexão e os segredos pelo ambiente da hospedagem. O Compose de produção usa PostgreSQL externo:

```bash
docker compose -f compose.prod.yaml up --build -d --wait
```

<h3>Primeiro acesso</h3>

Abra o [Swagger UI](http://localhost:8080/swagger-ui/index.html), execute `POST /auth/login` com o e-mail e a senha configurados para o administrador e copie o campo `token`. Em **Authorize**, informe apenas o token JWT para testar criação, atualização e remoção de produtos. As consultas públicas não exigem login.

A conta fica no banco daquele ambiente. Uma conta local não é transferida para a nuvem pelo Git ou pela imagem Docker. A branch `main` contém o código para produção; o merge não executa um deploy. Veja [operação e publicação](docs/operacao.md).

Para validar, com JDK 25 e Docker disponíveis:

```bash
./mvnw clean verify
```

Os testes usam PostgreSQL independente via Testcontainers. As configurações são carregadas dos arquivos `application*.properties`, e os segredos são fornecidos pelo processo, pela IDE ou pela hospedagem.

<h2 id="routes">📍 API Endpoints</h2>

Consultas públicas; operações administrativas usam `Authorization: Bearer <token>`.

| Rota | Descrição | Acesso |
| --- | --- | --- |
| <kbd>POST /auth/login</kbd> | Entrar com e-mail e senha e receber JWT | Público |
| <kbd>GET /produtos</kbd> | Listar, pesquisar por nome e filtrar por categoria | Público |
| <kbd>GET /produtos/{id}</kbd> | Consultar produto por ID | Público |
| <kbd>POST /produtos</kbd> | Adicionar produto | Admin |
| <kbd>PUT /produtos/{id}</kbd> | Modificar produto | Admin |
| <kbd>DELETE /produtos/{id}</kbd> | Remover produto | Admin |
| <kbd>GET /categorias</kbd> | Listar categorias | Público |

Consulte [contratos, exemplos e erros ProblemDetail](docs/api.md). A listagem aceita `nome`, `categoria`, `page` e `size`; JWT expira em 8 horas por padrão, sem refresh token.

<h3>Swagger</h3>

Com a aplicação em execução, acesse:

- [Swagger UI](http://localhost:8080/swagger-ui/index.html).
- [OpenAPI JSON](http://localhost:8080/v3/api-docs).

A documentação interativa descreve os endpoints, DTOs, validações e autenticação Bearer. Use o botão Authorize para testar as operações administrativas.

<h2 id="colab">🤝 Colaboradores</h2>

<table>
  <tr>
    <td align="center">
      <sub><b>Lucas Ferraz</b></sub><br>
      Desenvolvimento e manutenção
    </td>
  </tr>
</table>

<h2 id="contribute">📫 Contribuir</h2>

1. Crie sua branch a partir de `develop`: `git switch -c feature/nome-da-feature develop`.
2. Implemente a feature completa com validações, DTOs, mappers e testes relevantes.
3. Atualize os documentos em `docs/` e a documentação Swagger.
4. Faça um commit por feature, como `:sparkles: feat: adiciona cadastro de produtos`.
5. Abra um Pull Request para `develop`, explicando o comportamento e a validação realizada. A branch `main` é reservada à produção.

As regras completas de contribuição e implementação estão em [agents/README.md](agents/README.md).

<h3>Documentação do projeto</h3>

- [Arquitetura e domínio](docs/arquitetura.md).
- [Regras de negócio](docs/regras-de-negocio.md).
- [Contrato da API e Swagger](docs/api.md).
- [Ambiente de desenvolvimento e migrations](docs/desenvolvimento.md).
- [Operação, persistência e publicação](docs/operacao.md).
- [Orientações para agentes e contribuidores](agents/README.md).
