# Regras de negócio

[Voltar ao README](../README.md)

## Acesso

| Operação | Público | ADMIN | CLIENTE autenticado |
| --- | --- | --- | --- |
| Consultar, pesquisar e filtrar produtos | Sim | Sim | Sim |
| Consultar categorias | Sim | Sim | Sim |
| Adicionar, modificar e remover produtos | Não | Sim | Não |

As permissões são verificadas no servidor. Sem autenticação válida, a escrita retorna 401; com role sem permissão, retorna 403. O catálogo é público, mas enviar um Bearer inválido resulta em 401.

O administrador entra por e-mail e senha e recebe JWT com validade padrão de 8 horas. O primeiro administrador é criado por variáveis de ambiente, sem alterar contas existentes. Não há cadastro público nem CRUD de usuários/roles. CLIENTE permanece no modelo.

## Produtos e categorias

Produto possui nome, descrição opcional, preço em reais e uma categoria. Preço zero é permitido; valores negativos ou com mais de duas casas decimais são rejeitados. Nomes podem se repetir. PUT substitui os campos editáveis, e DELETE remove definitivamente o produto.

Categorias iniciais:

- BEBIDAS: bebidas não alcoólicas.
- BEBIDAS_ALCOOLICAS: bebidas alcoólicas.
- DOCES: inclui bombons e chicletes.
- SALGADOS.

Categorias são mantidas por enum e migrations; não há CRUD no aplicativo. A consulta de produtos é paginada e aceita pesquisa parcial por nome combinada com categoria.

Estoque, vendas, pagamentos, pedidos, imagens e recuperação de senha estão fora desta versão.

Consulte o [modelo de domínio](arquitetura.md#modelo-de-domínio) e o [contrato da API](api.md).
