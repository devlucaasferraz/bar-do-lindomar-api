# Arquitetura e domínio

[Voltar ao README](../README.md)

A aplicação segue a arquitetura abaixo. Autenticação e categorias estão implementadas; produtos serão adicionados nas próximas features.

## Arquitetura e organização

A aplicação segue MVC, com controllers para receber requisições, services para regras de negócio e repositories para persistência. Como se trata de uma API, a representação retornada ao aplicativo é JSON por meio de DTOs.

Os diretórios `src/models`, `src/models/dtos` e `src/models/mappers` solicitados são representados como pacotes dentro da raiz Java padrão do Maven, preservando a estrutura existente:

```text
src/
├── main/
│   ├── java/dev/lucasferraz/bardolindomarapi/
│   │   ├── BarDoLindomarApiApplication.java
│   │   ├── controllers/
│   │   ├── services/
│   │   ├── repositories/
│   │   ├── models/
│   │   │   ├── dtos/
│   │   │   ├── mappers/
│   │   │   └── enums/
│   │   ├── exceptions/
│   │   └── config/
│   └── resources/
│       ├── application.properties
│       └── db/migration/
└── test/java/dev/lucasferraz/bardolindomarapi/
```

Essa árvore descreve a organização prevista; os pacotes serão criados conforme as funcionalidades forem implementadas.

- **Controllers:** recebem parâmetros e DTOs, acionam validação e delegam aos services.
- **Services:** aplicam regras de negócio e delimitam transações.
- **Repositories:** acessam o banco com Spring Data JPA.
- **Models:** representam o domínio e seus relacionamentos.
- **DTOs:** definem os contratos de entrada e saída; entidades JPA não devem ser expostas diretamente.
- **Mappers:** convertem entidades e DTOs com MapStruct, integrados ao Spring.
- **Exceptions:** concentram exceções de domínio e o tratamento HTTP centralizado.
- **Config:** reúne configurações da aplicação, incluindo segurança.

## Modelo de domínio

Os campos abaixo são uma proposta inicial para implementação.

| Modelo | Dados básicos | Relacionamentos |
| --- | --- | --- |
| Produto | ID, nome, descrição opcional, preço | Pertence a uma categoria |
| Categoria | ID, código do enum, nome de exibição | Agrupa vários produtos |
| Usuário | ID, nome, e-mail, hash da senha | Possui uma ou mais roles |
| Role | ID, nome (`ADMIN` ou `CLIENTE`) | Pode estar associada a vários usuários |

O preço deve usar `BigDecimal` e uma coluna decimal no banco. Senhas devem ser armazenadas como hash e nunca retornar nos DTOs ou aparecer em logs.

### Categorias e enum

Para atender ao requisito de uma entidade `Categoria` e de um enum, a implementação mantém `Categoria` como entidade persistida e `TipoCategoria` como enum dos códigos permitidos. Cada categoria possui um código único, armazenado pelo nome do enum, nunca pela posição ordinal.

Categorias iniciais: `BEBIDAS` (não alcoólicas), `BEBIDAS_ALCOOLICAS`, `DOCES` (incluindo bombons e chicletes) e `SALGADOS`.

Para adicionar uma categoria:

1. Adicionar uma constante ao enum `TipoCategoria`.
2. Criar uma migration Flyway para inserir a categoria correspondente.
3. Conferir sua exposição no DTO de categoria e no filtro de produtos.

A inclusão exige uma nova versão da API. Não há necessidade de CRUD de categorias no aplicativo no escopo inicial. O aplicativo deve consumir a lista de categorias da API para evitar manter uma lista fixa própria.

### Validações, DTOs e mappers

Cada entidade deve ter DTOs próprios, validações aplicáveis e mapper MapStruct. DTOs de escrita devem existir apenas para operações expostas pela API; não é necessário criar CRUD de usuários ou roles para cumprir essa organização.

| Modelo | Validações previstas | DTOs previstos |
| --- | --- | --- |
| Produto | Nome obrigatório, preço obrigatório e não negativo, categoria existente | Criação, atualização e resposta |
| Categoria | Código e nome obrigatórios, código único e pertencente ao enum | Resposta; entrada validada se uma operação de escrita for adicionada |
| Usuário | Nome obrigatório, e-mail válido e único, senha obrigatória na criação | Entrada conforme o fluxo de autenticação definido e resposta sem senha |
| Role | Nome obrigatório, único e limitado a `ADMIN` e `CLIENTE` | Resposta; entrada validada se uma operação de escrita for adicionada |

Limites de tamanho e a política de senha devem ser definidos na implementação e refletidos nos DTOs e nas migrations. Restrições de integridade também devem existir no banco. Mappers devem ignorar campos controlados pelo servidor, como IDs de criação e hashes de senha.
