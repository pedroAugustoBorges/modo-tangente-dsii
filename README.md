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

O backend usará autenticação Spring Security. O Supabase será o provedor PostgreSQL, não um segundo provedor de login. Chaves e credenciais serão fornecidas por variáveis de ambiente; nenhum segredo real deve ser versionado.

## Integrantes

- Enzo Akira — autenticação, navegação e operação
- João Lima — catálogo de tópicos, quizzes e questões
- Pedro Augusto de Melo Borges — modelo integrado, tentativas, respostas, chat contextual e integração OpenRouter no backend
- Thiago G. Soares — interface Thymeleaf do estudante para quiz, questão, chat e histórico

## Estado do projeto

O baseline inicial contém a aplicação Spring Boot, nove entidades JPA e a migration Flyway V1 no schema `public`. A API já tem rotas protegidas para iniciar, listar e consultar tentativas, com consultas limitadas ao usuário persistido. Em 20/11/2026, o perfil `supabase` conectou ao PostgreSQL hospedado no Supabase, aplicou a migration V1 com sucesso e iniciou a aplicação. Para usar as rotas com contas reais, ainda falta Enzo integrar o login para que o principal Spring Security represente o e-mail de `AppUser`. Catálogo/CRUD, correção de questões, chat, telas do quiz, integração OpenRouter por Pedro e publicação continuam pendentes.

## Execução local

Requer Java 17. Execute os testes com `./mvnw test` (Windows: `mvnw.cmd test`). O perfil local usa H2 em arquivo para desenvolvimento.

Para testar com o Supabase, copie `.env.example` para `.env` e preencha `SUPABASE_JDBC_URL`, `SUPABASE_DB_USER` e `SUPABASE_DB_PASSWORD` com os dados da opção de conexão escolhida no Supabase. O `.env` é local e ignorado pelo Git. No PowerShell, ative o perfil e inicie a aplicação:

```powershell
$env:SPRING_PROFILES_ACTIVE = "supabase"
.\mvnw.cmd spring-boot:run
```

O perfil `supabase` carrega o `.env` e executa as migrations Flyway no schema `public` ao iniciar. Use uma base de teste vazia: a migration V1 criará as tabelas do projeto. Não coloque credenciais reais no código nem as envie pelo chat.

## Documentação

- Escopo e comportamento: [docs/escopo.md](docs/escopo.md)
- Migrations: [src/main/resources/db/migration](src/main/resources/db/migration)
- API OpenAPI: a configurar
- Aplicação publicada: a definir
