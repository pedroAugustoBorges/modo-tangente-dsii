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

O backend usa autenticação Spring Security. O Supabase é o provedor PostgreSQL, não um segundo provedor de login. Chaves e credenciais são lidas de variáveis de ambiente ou do `.env` local; nenhum segredo real deve ser versionado.

## Integrantes

- Enzo Akira — autenticação, navegação e operação
- João Lima — catálogo de tópicos, quizzes e questões
- Pedro Augusto de Melo Borges — modelo integrado, tentativas, respostas, chat contextual e integração OpenRouter no backend
- Thiago G. Soares — interface Thymeleaf do estudante para quiz, questão, chat e histórico

## Estado do projeto

O projeto já contém as nove entidades JPA, as migrations Flyway V1 e V2 e as APIs de tentativas, progresso por questão, conversas e mensagens. O backend também tem um adaptador OpenRouter com prompt contextual, limites de histórico, timeout e falhas sanitizadas; os testes simulam a API e não usam a chave real. O uso do chat com o catálogo depende do bean de produção `QuizContextProvider` e o início de tentativas depende do `QuizQuestionProvider`, ambos da integração de João. Também seguem pendentes a autenticação integrada de Enzo, as telas completas, o deploy e a validação final com os provedores reais.

## Execução local

Requer Java 17. Execute os testes com `./mvnw test` (Windows: `mvnw.cmd test`). O perfil local usa H2 em arquivo para desenvolvimento e carrega opcionalmente o `.env` para a configuração do OpenRouter.

Para configurar o tutor, preencha `OPENROUTER_API_KEY` e, se necessário, ajuste `OPENROUTER_MODEL` no `.env`. O `.env` é local e ignorado pelo Git. Para testar com o Supabase, preencha também `SUPABASE_JDBC_URL`, `SUPABASE_DB_USER` e `SUPABASE_DB_PASSWORD` com os dados da opção de conexão escolhida no Supabase. No PowerShell, ative o perfil e inicie a aplicação:

```powershell
$env:SPRING_PROFILES_ACTIVE = "supabase"
.\mvnw.cmd spring-boot:run
```

O perfil `supabase` carrega o `.env` e executa as migrations Flyway no schema `public` ao iniciar. Use uma base de teste vazia: a V1 cria as tabelas e a V2 adiciona o controle de recuperação de mensagens em processamento. Não coloque credenciais reais no código nem as envie pelo chat.

## Documentação

- Escopo e comportamento: [docs/escopo.md](docs/escopo.md)
- Migrations: [src/main/resources/db/migration](src/main/resources/db/migration)
- API OpenAPI: [docs/openapi.yaml](docs/openapi.yaml)
- Aplicação publicada: a definir
