# Neon e deploy na Render

[Voltar ao README](../README.md)

## Arquitetura

O Neon fornece o PostgreSQL. A API continua Java/Spring Boot, hospedada como contêiner Docker na Render. JPA, o driver PostgreSQL JDBC, JWT e migrations Flyway permanecem na aplicação. Não há Neon Auth, Functions, Data API ou armazenamento de objetos configurados.

A conexão da API usa DATABASE_URL (pooled); o Flyway usa DATABASE_URL_UNPOOLED (direta). A aplicação converte as URLs PostgreSQL do Neon para JDBC, separa usuário e senha e preserva parâmetros TLS. Não passe a URL PostgreSQL bruta para DB_URL, que continua aceitando apenas JDBC.

O pool de produção mantém no máximo 5 conexões por padrão (DB_POOL_SIZE), mínimo zero, sem keepalive e com fechamento de conexões ociosas após 60 segundos.

## CLI e vínculo local

```bash
npm i -g neon@latest
neon init -y --org-id org-proud-leaf-20758192 --project-id cold-shadow-68841189 --branch production --services postgres
neon deploy
```

Se necessário, conclua a autenticação no navegador com `neon login`. O arquivo neon.ts versiona a política. O vínculo .neon e as credenciais .env.local geradas pela CLI ficam fora do Git. Esse arquivo é uma saída local da CLI; a configuração Spring continua nos perfis application*.properties.

A CLI pode escrever também NEON_BRANCH, NEON_BRANCH_ID e NEON_FUNCTION_HOST_SUFFIX. São metadados; nenhum serviço Functions foi provisionado.

## Build e execução local com Neon

JDK 25, Node.js 22 ou superior e Docker são necessários para o build completo com testes. npm é somente uma interface para as ferramentas; o backend não foi convertido para Node.js.

```bash
npm ci
npm run build
```

`npm run build` executa `./mvnw clean verify`, compila a API e roda testes com PostgreSQL independente via Testcontainers. Não usa o banco Neon de produção.

Para executar a API com as credenciais baixadas:

```bash
npm start
```

O comando carrega .env.local (ou NEON_ENV_FILE), preserva variáveis já fornecidas pelo processo, seleciona prod por padrão e executa o JAR construído. Configure JWT_SECRET no processo e ADMIN_NOME, ADMIN_EMAIL e ADMIN_SENHA se ainda não houver administrador. Nenhuma conta administrativa permanente é criada pelas ferramentas Neon.

Na hospedagem e ao executar diretamente pelo Java, a aplicação lê variáveis do processo; não carrega arquivos de credenciais automaticamente.

## Teste real ponta a ponta

```bash
npm run test:neon
```

Esse comando inicia a aplicação com perfil prod e servidor HTTP local em porta aleatória. Executa Flyway na conexão direta e testa login JWT, consulta pública de categorias, criação/consulta/atualização/exclusão de produto persistido, pesquisa e Swagger na conexão pooled. Cria um administrador de teste temporário, removido ao final, sem redefinir contas existentes.

Prefira testar primeiro em uma branch Neon temporária derivada de production. Use `neon env pull --branch NOME --service postgres --file CAMINHO` e defina NEON_ENV_FILE para apontar ao arquivo separado. O vínculo .neon não precisa ser alterado. Depois, valide a branch de destino.

O teste aplica migrations no banco selecionado. O usuário e o produto temporários são removidos ao encerrar; schema e dados de referência das migrations permanecem. Não execute contra outro projeto sem conferir o vínculo e as URLs.

## Render: passo a passo

1. Publique no GitHub o commit da integração, na branch main.
2. No Neon, abra o projeto vinculado e selecione a branch production. Em Connect, obtenha as URLs pooled e direta, da mesma database e role.
3. Na Render, crie um Web Service a partir do repositório, com branch main, runtime Docker, Dockerfile ./Dockerfile e contexto na raiz.
4. Selecione a região mais próxima da região do projeto Neon. A conexão com Neon é externa, não o endereço interno de um Postgres da Render.
5. Configure as variáveis abaixo no serviço da API.
6. Defina Health Check Path como /categorias. Mantenha o comando Docker padrão; a imagem já tem ENTRYPOINT.
7. Inicie o deploy. Flyway aplica migrations pendentes e o bootstrap cria o primeiro administrador, se necessário.
8. Acesse /categorias, /produtos e /swagger-ui/index.html na URL HTTPS fornecida pela Render. Faça login por /auth/login e use Authorize no Swagger com apenas o token JWT.

| Variável da API | Valor |
| --- | --- |
| SPRING_PROFILES_ACTIVE | prod |
| PORT | 8080 |
| DATABASE_URL | URL pooled completa do Neon, começando com postgresql:// e contendo -pooler no host |
| DATABASE_URL_UNPOOLED | URL direta completa, da mesma database/role, sem -pooler no host |
| JWT_SECRET | Segredo aleatório de pelo menos 32 bytes UTF-8 |
| ADMIN_NOME | Nome escolhido para o primeiro administrador |
| ADMIN_EMAIL | E-mail escolhido para o primeiro administrador |
| ADMIN_SENHA | Senha escolhida, mínimo 8 caracteres e máximo 72 bytes UTF-8 |
| JWT_TTL_SECONDS | Opcional; padrão 28800 |
| DB_POOL_SIZE | Opcional; padrão 5 |

Copie as URLs do arquivo local sem aspas externas ou obtenha-as em Connect; não publique as credenciais. Preserve sslmode e demais parâmetros fornecidos. Gere JWT_SECRET, por exemplo, com `openssl rand -hex 32`.

Não é necessário definir DB_URL, DB_USER ou DB_PASSWORD ao usar as duas URLs Neon. Se essas variáveis já existirem na Render de uma configuração anterior, remova-as antes do deploy: valores explícitos DB_* têm prioridade sobre os derivados do Neon. DB_MIGRATIONS_URL/USER/PASSWORD também são sobrescritas explícitas opcionais.

O compose.prod.yaml também aceita as duas URLs Neon no ambiente do processo que executa o Compose; valores DB_* vazios são tratados como ausentes. Sem Neon, continua aceitando a configuração JDBC explícita.

A Render usa o Dockerfile, não o Compose. Não é necessário executar neon deploy durante o build da Render, instalar Node no contêiner Java ou configurar um comando npm no serviço.

O administrador pertence ao banco. Alterar ADMIN_* depois da criação não muda a conta existente. Não há recuperação de senha por endpoint nesta versão.

## Referências

- [Métodos de conexão Neon](https://neon.com/docs/connect/choose-connection).
- [Pooling e conexões diretas](https://neon.com/docs/connect/connection-pooling).
- [Web Services na Render](https://render.com/docs/web-services).
