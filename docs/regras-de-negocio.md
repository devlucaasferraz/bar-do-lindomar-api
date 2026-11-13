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

Controle de estoque, vendas, pagamentos e pedidos estão fora do escopo inicial. O mecanismo de autenticação, o cadastro de clientes e o provisionamento do primeiro administrador ainda precisam ser definidos. O cadastro de um usuário não deve permitir que ele atribua a si mesmo a role de administrador.


Consulte o [modelo de domínio](arquitetura.md#modelo-de-domínio) para campos, relacionamentos, categorias e validações.
