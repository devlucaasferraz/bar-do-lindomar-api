# Contrato da API

[Voltar ao README](../README.md)

## Contrato HTTP proposto

As rotas abaixo são uma proposta para orientar as features. Ainda não existem controllers implementando esse contrato.

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
GET /produtos?nome=cerveja&categoria=CERVEJAS
```

Uma pesquisa sem resultados retorna uma coleção vazia com status `200`. Um ID inexistente retorna `404`. A criação retorna `201` com o produto criado e o cabeçalho `Location`; a atualização retorna `200` com o produto atualizado; a remoção retorna `204`, sem corpo.

## Autenticação implementada

`POST /auth/login` recebe `{"email":"admin@exemplo.com","senha":"sua-senha"}` e retorna `token`, `tipo` (`Bearer`), `expiraEm` em segundos e `usuario` com ID, nome, e-mail e roles. O usuário nunca contém senha ou hash.

Enviar `Authorization: Bearer <token>` para operações administrativas. JWT usa HS256, emissor `bar-do-lindomar-api` e validade padrão de 8 horas, configurável por `JWT_TTL_SECONDS`. Não há refresh token: ao expirar, fazer novo login. Roles são carregadas no token; mudanças de permissões só passam a valer após expiração dos tokens existentes. Credenciais inválidas retornam `401` sem revelar a existência da conta.

Catálogo público: consultas não exigem conta. Escrita exige `ADMIN`. `CLIENTE` permanece no modelo, sem cadastro ou CRUD público. Rotas não previstas são negadas.

O formato final das respostas de listagem será definido na feature de consulta.

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
