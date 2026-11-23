# Modo Tangente

Tutor web de apoio aos estudos da disciplina Desenvolvimento de Servidores II. O aluno escolhe um tópico, responde quizzes de múltipla escolha e pode conversar com um tutor sobre cada questão. O contexto enviado ao modelo é montado no servidor a partir do quiz, da questão e do histórico persistidos.

As respostas da IA terão um tom descontraído inspirado na forma de explicar e no humor do professor Felipe Cannarrozo Lourenço, sem afirmar que o modelo é o professor nem atribuir a ele falas inventadas.

## Tecnologias

- Java 17 e Spring Boot
- Spring MVC, Spring Data JPA e Hibernate
- PostgreSQL hospedado no Supabase
- Thymeleaf para a interface web
- Flyway para migrations versionadas
- OpenRouter para integração com modelos de linguagem

O Spring Security autentica as contas do Modo Tangente armazenadas no PostgreSQL do Supabase. Chaves e credenciais são lidas de variáveis de ambiente ou do `.env` local, que é ignorado pelo Git.

## Integrantes

- Enzo Akira — autenticação, navegação e operação
- João Lima — catálogo de tópicos, quizzes e questões
- Pedro Augusto de Melo Borges — modelo integrado, tentativas, respostas, chat contextual e integração OpenRouter no backend
- Thiago G. Soares — interface Thymeleaf do estudante para quiz, questão, chat e histórico

## Estado do projeto

O projeto contém as entidades de estudo e o registro de importações, as migrations Flyway V1 a V4 e as APIs de catálogo, tentativas, respostas, conversas e mensagens. O login usa `AppUser`, senhas com BCrypt e os perfis `STUDENT` e `ADMIN`. A sessão pode ser consultada em `GET /api/me`. Login, início, catálogo e questão usam o layout Thymeleaf compartilhado.

O catálogo fornece as questões publicadas para novas tentativas e o contexto persistido para o tutor. As rotas com dados das telas de estudo, as páginas administrativas e a publicação ainda estão pendentes.

## Catálogo

Administradores criam e atualizam tópicos e quizzes em `/api/admin/topics` e `/api/admin/quizzes`. Em `/api/admin/quizzes/{quizId}/questions`, o corpo da questão inclui a lista completa de alternativas. Use POST para criar e PUT no ID para substituir enunciado, alternativas e ordem em uma transação. Um rascunho aceita uma lista vazia. Publicar exige pelo menos duas alternativas e uma correta. DELETE desativa o recurso. Quando há tentativa associada, PUT devolve o ID da nova versão e a anterior continua disponível na tentativa original.

A leitura do aluno parte de `GET /api/topics`, passa por `GET /api/topics/{topicId}/quizzes` e termina em `GET /api/quizzes/{quizId}/questions`. O histórico usa `GET /api/attempts/{attemptId}/questions/{questionId}` com a sessão do dono da tentativa. O gabarito é usado na correção do servidor. Apenas a administração autenticada tem acesso aos marcadores de correção ao editar alternativas.

Para carregar o exemplo de injeção de dependência já presente nos testes do grupo, defina `CATALOG_IMPORT_RESOURCE=classpath:catalog/spring-ioc.json` ao iniciar a aplicação. Para material externo, use `file:/caminho/catalogo.json` com o mesmo formato e uma origem versionada no campo `source`. A importação é transacional e registra a origem e o SHA-256 dos bytes do arquivo. Reexecutar o mesmo lote preserva os IDs. Um arquivo diferente com a mesma origem é recusado.

## Execução local

Requer Java 17. Execute os testes com `./mvnw test` em Linux ou macOS, ou `.\mvnw.cmd test` no PowerShell. A suíte usa H2 em memória e simula o catálogo e o OpenRouter nos cenários que dependem deles.

`./mvnw spring-boot:run` inicia o perfil local, que usa H2 em arquivo. Para usar PostgreSQL, copie `.env.example` para `.env`, preencha as variáveis do Supabase com os dados de uma única opção de conexão e inicie com o perfil `supabase`. Exemplo no PowerShell.

```powershell
$env:SPRING_PROFILES_ACTIVE = "supabase"
.\mvnw.cmd spring-boot:run
```

## Preparação do deploy

1. Gere o pacote com `./mvnw clean verify` ou `.\mvnw.cmd clean verify` no PowerShell. O executável fica em `target/modo-tangente-dsii-0.1.0-SNAPSHOT.jar`.
2. Defina `SPRING_PROFILES_ACTIVE=supabase`, `SUPABASE_JDBC_URL`, `SUPABASE_DB_USER` e `SUPABASE_DB_PASSWORD` no ambiente da hospedagem. Configure `OPENROUTER_API_KEY` para usar o tutor e ajuste `OPENROUTER_MODEL` se necessário. A porta padrão é 8080 e pode ser alterada com `SERVER_PORT`.
3. Inicie o pacote com `java -jar target/modo-tangente-dsii-0.1.0-SNAPSHOT.jar`. O Flyway aplica as migrations no schema `public` antes da validação do Hibernate. Use um PostgreSQL vazio e isolado para validar as migrations antes de apontar para o banco compartilhado.
4. Configure `GET /actuator/health` como verificação de saúde da aplicação. Depois, abra `/login` e valide a sessão com uma conta `AppUser` ativa, provisionada com BCrypt e o perfil correspondente. Confirme a integração de quiz e chat antes de registrar o endereço publicado.

## Documentação

- Escopo e comportamento: [docs/escopo.md](docs/escopo.md)
- Migrations: [src/main/resources/db/migration](src/main/resources/db/migration)
- API OpenAPI: [docs/openapi.yaml](docs/openapi.yaml)
- Aplicação publicada: a definir
