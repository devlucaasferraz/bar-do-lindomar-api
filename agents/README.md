# Orientações para agentes e contribuidores

[Voltar ao README](../README.md)

## Referências

- [Arquitetura, entidades, DTOs e mappers](../docs/arquitetura.md)
- [Regras de negócio e permissões](../docs/regras-de-negocio.md)
- [Contrato HTTP, ProblemDetail e Swagger](../docs/api.md)
- [Ambiente, testes e migrations](../docs/desenvolvimento.md)

## Regras de implementação

Manter a solução simples e seguir MVC. As entidades ficam no pacote `models`, os DTOs em `models/dtos` e os mappers MapStruct em `models/mappers`, dentro de `src/main/java/dev/lucasferraz/bardolindomarapi`.

Cada entidade deve possuir validações, DTOs e mappers adequados às operações expostas. Concentrar regras nos services e tratar exceções de forma centralizada com `ProblemDetail`. Somente administradores podem adicionar, modificar e remover produtos. O catálogo e as categorias são públicos; CLIENTE permanece no modelo sem permissão de escrita. Não adicionar cadastro ou CRUD de usuários sem ampliar explicitamente o escopo.

Usar PostgreSQL e migrations Flyway. Novas categorias devem seguir o enum e a entidade definidos na arquitetura. Adicionar comentários somente quando necessários para melhorar a legibilidade.

Atualizar os documentos em `docs/` e as anotações Swagger na mesma feature que alterar comportamento ou contrato. Manter propostas e funcionalidades implementadas claramente identificadas. Não registrar credenciais reais na documentação ou no código.

## Fluxo de desenvolvimento

- `main`: branch de produção.
- `develop`: branch de desenvolvimento e integração.
- `feature/nome-da-feature`: branch de uma feature, criada a partir de `develop`.
- Uma feature completa corresponde a um commit.

Fluxo após a criação das branches base:

```bash
git switch develop
git pull --ff-only
git switch -c feature/cadastro-de-produtos
```

Ao concluir a implementação, validar a feature e adicionar somente os arquivos relacionados antes do commit. Usar o padrão de mensagens com emoji textual:

```text
:sparkles: feat: adiciona cadastro de produtos
:bug: fix: corrige filtro de produtos por categoria
:memo: docs: documenta arquitetura e regras do projeto
:recycle: refactor: simplifica mapeamento de produtos
:white_check_mark: test: valida permissões de produtos
```

Integrar a feature em `develop`, preservando um único commit por feature; usar squash quando houver commits intermediários. Publicações em produção partem de `develop` para `main`.

A documentação e as features aprovadas são integradas em `develop`. `main` permanece reservada à publicação. Não há remoto configurado neste repositório; só executar pull/push depois de configurar um remoto.

### Critérios de conclusão de uma feature

- Regras de negócio e permissões implementadas.
- DTOs, validações e mappers das entidades envolvidas disponíveis.
- Erros tratados no padrão `ProblemDetail`.
- Migration incluída quando houver alteração de schema ou dados de referência.
- Testes relevantes passando e documentação atualizada.
- Comentários somente quando necessários para explicar código cuja intenção não esteja clara.
- Um commit representando a feature completa.
