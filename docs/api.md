# Contrato da API

[Voltar ao README](../README.md)

## Contrato HTTP

As rotas de produtos, categorias e autenticação estão implementadas.

| Método | Rota | Operação | Acesso |
| --- | --- | --- | --- |
| `GET` | `/produtos` | Listar e pesquisar produtos | Admin e cliente |
| `GET` | `/produtos/{id}` | Consultar um produto | Admin e cliente |
| `POST` | `/produtos` | Criar um produto | Admin |
| `PUT` | `/produtos/{id}` | Atualizar um produto | Admin |
| `DELETE` | `/produtos/{id}` | Remover um produto | Admin |
| `GET` | `/categorias` | Listar categorias disponíveis | Admin e cliente |

A listagem deve aceitar `nome` para pesquisa parcial e `categoria` para filtrar pelo código do enum. Os filtros podem ser combinados:

```http
GET /produtos?nome=cerveja&categoria=BEBIDAS_ALCOOLICAS
```

Uma pesquisa sem resultados retorna uma página com `items` vazio e status `200`. Um ID inexistente retorna `404`. A criação retorna `201` com o produto criado e o cabeçalho `Location`; a atualização retorna `200` com o produto atualizado; a remoção retorna `204`, sem corpo.

## Autenticação implementada

`POST /auth/login` recebe `{"email":"admin@exemplo.com","senha":"sua-senha"}` e retorna `token`, `tipo` (`Bearer`), `expiraEm` em segundos e `usuario` com ID, nome, e-mail e roles. O usuário nunca contém senha ou hash.

Enviar `Authorization: Bearer <token>` para operações administrativas. JWT usa HS256, emissor `bar-do-lindomar-api` e validade padrão de 8 horas, configurável por `JWT_TTL_SECONDS`. Não há refresh token: ao expirar, fazer novo login. Roles são carregadas no token; mudanças de permissões só passam a valer após expiração dos tokens existentes. Credenciais inválidas retornam `401` sem revelar a existência da conta.

Catálogo público: consultas não exigem conta. Escrita exige `ADMIN`. `CLIENTE` permanece no modelo, sem cadastro ou CRUD público. Rotas não previstas são negadas.

O contrato de paginação está descrito abaixo.

## Tratamento de erros

O tratamento deve ser centralizado com `@RestControllerAdvice`, utilizando `ProblemDetail` e o tipo de conteúdo `application/problem+json`.

| Status | Situação |
| --- | --- |
| `400 Bad Request` | Dados inválidos, enum inválido ou requisição malformada |
| `401 Unauthorized` | Ausência de autenticação válida |
| `403 Forbidden` | Usuário autenticado sem permissão |
| `404 Not Found` | Recurso não encontrado |
| `409 Conflict` | Violação de unicidade ou conflito de integridade |
| `500 Internal Server Error` | Erro inesperado |

Exemplo de resposta de validação prevista:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Um ou mais campos são inválidos.",
  "instance": "/produtos",
  "errors": [
    {
      "field": "nome",
      "message": "O nome é obrigatório."
    }
  ]
}
```

Erros de autenticação e autorização devem seguir o mesmo formato, inclusive quando tratados pela camada de segurança antes do controller. Respostas não devem expor stack traces, SQL, senhas ou detalhes internos.


## Documentação via Swagger

Com a aplicação em execução:

- [Swagger UI](http://localhost:8080/swagger-ui/index.html)
- [OpenAPI JSON](http://localhost:8080/v3/api-docs)

Os metadados da API são definidos em `config/OpenApiConfig.java`. O Springdoc gera o contrato a partir dos controllers e DTOs implementados. Enquanto não houver endpoints de negócio, eles não aparecerão no Swagger.

A cada feature, documentar no Swagger:

- Operação e finalidade da rota, com `@Operation` e `@Tag`.
- Parâmetros, filtros e exemplos de entrada e saída.
- Códigos de sucesso e de erro com `@ApiResponse`, incluindo `ProblemDetail`.
- Campos e restrições dos DTOs com `@Schema` e Bean Validation.
- Requisitos de autenticação e autorização, quando o mecanismo de segurança for implementado.

Os exemplos e as propostas deste documento devem ser atualizados junto com o contrato implementado. Não declarar no Swagger rotas ou esquemas de autenticação ainda inexistentes.

## Categorias implementadas

`GET /categorias` é público e retorna um array de objetos com `id`, `codigo` e `nome`, ordenado por nome. Códigos iniciais: `BEBIDAS`, `BEBIDAS_ALCOOLICAS`, `DOCES` e `SALGADOS`. Bombons/chicletes pertencem a DOCES. Não há CRUD de categorias.

## Manutenção de produtos implementada

POST e PUT recebem `nome` (obrigatório, até 120), `descricao` (opcional, até 500), `preco` (não negativo, até 10 dígitos inteiros e 2 decimais, em reais) e `categoriaId` (ID positivo existente). Exemplo:

```json
{"nome":"Refrigerante lata","descricao":"350 ml","preco":5.50,"categoriaId":1}
```

A resposta contém `id`, os campos do produto e `categoria` com ID, código e nome. PUT substitui os campos editáveis; descrição omitida torna-se nula. Nomes podem se repetir. POST retorna 201 e Location; PUT, 200; DELETE físico, 204. Produto ou categoria inexistente retorna 404. Identificadores e dados inválidos retornam 400.

## Consulta pública e paginação

GET `/produtos` aceita `nome` (até 120 caracteres; trecho literal sem diferenciar maiúsculas/minúsculas), `categoria` (código exato do enum), `page` (a partir de 0) e `size` (1 a 100, padrão 20). Filtros combinam-se com E. Nome vazio não filtra; percentuais e sublinhados são literais, não curingas. Ordenação fixa por nome e ID.

```json
{"items":[],"page":0,"size":20,"totalElements":0,"totalPages":0}
```

Cada item contém `id`, `nome`, `descricao`, `preco` e `categoria` (`id`, `codigo`, `nome`). Página além do fim retorna items vazio e mantém os totais. Parâmetros inválidos retornam 400. GET `/produtos/{id}` retorna produto ou 404. Ambas as consultas são públicas.
