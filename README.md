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

- JDK 25 com `JAVA_HOME` configurado.
- Git.
- Docker com Docker Compose.

<h3>Clonando</h3>

O repositório ainda não possui remoto configurado. Substitua `<URL_DO_REPOSITORIO>` pela URL quando estiver disponível:

```bash
git clone <URL_DO_REPOSITORIO>
cd bar-do-lindomar-api
```

<h3>Configuração do ambiente</h3>

Copie `.env.example` para `.env`. Defina senha do PostgreSQL, nome/e-mail/senha do administrador inicial e `JWT_SECRET` aleatório com pelo menos 32 bytes. Gere o segredo com `openssl rand -hex 32`.

```bash
cp .env.example .env
```

Edite os valores antes de iniciar. O administrador é criado apenas se ainda não existir; mudar a senha no ambiente depois não altera a conta persistida.

<h3>Iniciando</h3>

```bash
docker compose up --build -d
```

API em `http://localhost:8080`, com PostgreSQL persistido em volume. Para executar diretamente com Maven, exporte as variáveis da aplicação descritas em [ambiente de desenvolvimento](docs/desenvolvimento.md) e use `./mvnw spring-boot:run`.

Para validar, com JDK 25 e Docker disponíveis:

```bash
./mvnw clean verify
```

Os testes usam PostgreSQL independente via Testcontainers. O `.env` é carregado pelo Compose; a execução direta do Spring exige variáveis exportadas ou configuradas na IDE.

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
- [Orientações para agentes e contribuidores](agents/README.md).
