# Contrato da API

[Voltar ao README](../README.md)

## Swagger e OpenAPI

Com a aplicação em execução:

- [Swagger UI](http://localhost:8080/swagger-ui/index.html).
- [OpenAPI JSON](http://localhost:8080/v3/api-docs).

Ajuste a porta se necessário. O Springdoc gera o contrato dos controllers e DTOs; os metadados ficam em `config/OpenApiConfig.java`. Cada feature deve atualizar anotações, exemplos, respostas e estes documentos no mesmo commit.

## Rotas

| Método | Rota | Operação | Acesso |
| --- | --- | --- | --- |
| POST | `/auth/login` | Obter token | Público |
| GET | `/categorias` | Listar categorias | Público |
| GET | `/produtos` | Listar, pesquisar e filtrar | Público |
| GET | `/produtos/{id}` | Consultar produto | Público |
| POST | `/produtos` | Criar produto | ADMIN |
| PUT | `/produtos/{id}` | Substituir dados editáveis | ADMIN |
| DELETE | `/produtos/{id}` | Remover produto | ADMIN |

IDs são inteiros positivos. A API usa JSON e valores monetários numéricos em reais. Exemplos abaixo são ilustrativos; obtenha IDs reais nas consultas.

## POST /auth/login

Requisição:

```json
{
  "email": "admin@exemplo.com",
  "senha": "sua-senha"
}
```

Resposta `200`:

```json
{
  "token": "<jwt>",
  "tipo": "Bearer",
  "expiraEm": 28800,
  "usuario": {
    "id": 1,
    "nome": "Administrador",
    "email": "admin@exemplo.com",
    "roles": [{"id": 1, "nome": "ADMIN"}]
  }
}
```

E-mail é normalizado em minúsculas. Campos obrigatórios ou formato inválido retornam 400; credenciais incorretas retornam 401 com mensagem genérica. A resposta não expõe senha ou hash.

Enviar `Authorization: Bearer <token>` nas operações administrativas. JWT usa HS256 e emissor `bar-do-lindomar-api`. O segredo é externo; a validade padrão é 8 horas e pode ser alterada por `JWT_TTL_SECONDS`. Não há refresh token ou logout com revogação; ao expirar, fazer novo login. Roles são carregadas no token, portanto mudanças de permissão só afetam novos tokens. Trocar o segredo invalida todos os tokens anteriores.

Consultas não exigem conta. Enviar token inválido ou expirado, mesmo em consulta pública, retorna 401. Não há cadastro público nem CRUD de usuários/roles.

## GET /categorias

Retorna `200` com array ordenado por nome:

```json
[
  {"id": 1, "codigo": "BEBIDAS", "nome": "Bebidas"},
  {"id": 2, "codigo": "BEBIDAS_ALCOOLICAS", "nome": "Bebidas alcoólicas"},
  {"id": 3, "codigo": "DOCES", "nome": "Doces"},
  {"id": 4, "codigo": "SALGADOS", "nome": "Salgados"}
]
```

BEBIDAS exclui alcoólicas; DOCES inclui bombons e chicletes. Não há CRUD de categorias. Para adicionar, atualizar o enum e incluir uma migration.

## POST /produtos e PUT /produtos/{id}

Requisição:

```json
{
  "nome": "Refrigerante lata",
  "descricao": "Lata de 350 ml",
  "preco": 5.50,
  "categoriaId": 1
}
```

| Campo | Regra |
| --- | --- |
| nome | Obrigatório, não vazio, até 120 caracteres; espaços externos são removidos |
| descricao | Opcional, até 500 caracteres |
| preco | Obrigatório, não negativo, até 10 dígitos inteiros e 2 casas decimais |
| categoriaId | Obrigatório, ID positivo de categoria existente |

POST retorna `201`, cabeçalho `Location: /produtos/{id}` e o produto criado. PUT retorna `200` com o produto atualizado. PUT substitui todos os campos editáveis; descrição omitida torna-se nula. Nomes podem se repetir; preço zero é permitido.

Resposta:

```json
{
  "id": 1,
  "nome": "Refrigerante lata",
  "descricao": "Lata de 350 ml",
  "preco": 5.50,
  "categoria": {"id": 1, "codigo": "BEBIDAS", "nome": "Bebidas"}
}
```

Produto ou categoria inexistente retorna 404; dados inválidos retornam 400. O ID do produto é gerado pelo servidor e não é alterado no PUT.

## DELETE /produtos/{id}

Remove fisicamente o produto. Sucesso retorna `204`, sem corpo. Produto inexistente, inclusive uma segunda exclusão do mesmo ID, retorna 404.

## GET /produtos/{id}

Retorna `200` com a mesma representação de produto mostrada acima. Produto inexistente retorna 404; ID inválido retorna 400.

## GET /produtos

| Parâmetro | Regra |
| --- | --- |
| nome | Opcional, até 120 caracteres, pesquisa parcial sem diferenciar maiúsculas/minúsculas |
| categoria | Opcional, código exato do enum |
| page | Inteiro a partir de 0; padrão 0 |
| size | Inteiro de 1 a 100; padrão 20 |

Filtros combinam-se com E. Nome vazio ou apenas espaços não filtra. Percentuais e sublinhados são literais, não curingas. A pesquisa não remove acentos. Ordenação fixa por nome segundo a ordenação do banco e depois por ID.

```http
GET /produtos?nome=refrigerante&categoria=BEBIDAS&page=0&size=20
```

Resposta `200`:

```json
{
  "items": [
    {
      "id": 1,
      "nome": "Refrigerante lata",
      "descricao": "Lata de 350 ml",
      "preco": 5.50,
      "categoria": {"id": 1, "codigo": "BEBIDAS", "nome": "Bebidas"}
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Sem resultados, `items` é vazio e os totais são zero. Página além do fim retorna `items` vazio e mantém os totais da pesquisa. Parâmetros inválidos retornam 400.

## Erros ProblemDetail

O tratamento é centralizado com `@RestControllerAdvice`; falhas de segurança seguem o mesmo padrão. O tipo de conteúdo é `application/problem+json`.

| Status | Situação |
| --- | --- |
| 400 | Dados inválidos, JSON malformado, enum/ID/filtros/paginação inválidos |
| 401 | Ausência de autenticação necessária, credenciais ou token inválidos/expirados |
| 403 | Usuário autenticado sem permissão |
| 404 | Produto ou categoria não encontrado |
| 409 | Conflito de integridade |
| 500 | Erro inesperado, sem detalhes internos |

Exemplo de validação de corpo:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Um ou mais campos são inválidos.",
  "instance": "/produtos",
  "errors": [{"field": "nome", "message": "must not be blank"}]
}
```

As mensagens de Bean Validation podem variar com o locale do ambiente. A extensão `errors` é retornada nas validações do corpo; erros de parâmetros podem usar apenas `detail`. Respostas não expõem stack traces, SQL, senhas ou hashes.
