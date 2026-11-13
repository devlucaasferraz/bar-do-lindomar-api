# Regras de negócio

[Voltar ao README](../README.md)

## Escopo e permissões

| Operação | Admin | Cliente |
| --- | --- | --- |
| Listar produtos | Sim | Sim |
| Buscar produto | Sim | Sim |
| Filtrar produtos por categoria | Sim | Sim |
| Adicionar produto | Sim | Não |
| Modificar produto | Sim | Não |
| Remover produto | Sim | Não |

A API deve verificar as permissões no servidor em todas as operações de escrita. O aplicativo mobile não substitui essa validação.

Controle de estoque, vendas, pagamentos e pedidos estão fora do escopo inicial. O catálogo é público. O administrador entra por e-mail e senha e recebe um JWT. O primeiro administrador é criado por variáveis de ambiente, sem alterar contas existentes. Não há cadastro de clientes ou CRUD de usuários nesta versão. A role CLIENTE é mantida no modelo, sem permissão de escrita.


Consulte o [modelo de domínio](arquitetura.md#modelo-de-domínio) para campos, relacionamentos, categorias e validações.
