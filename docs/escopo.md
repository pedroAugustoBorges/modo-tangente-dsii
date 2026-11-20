# Escopo do Modo Tangente

## Objetivo

Apoiar alunos de Desenvolvimento de Servidores II em Java, orientação a objetos, Reflection, Servlets, Spring, Spring Boot Web e JPA. A aplicação combina tópicos de estudo, quizzes de múltipla escolha e um tutor conversacional contextualizado por questão.

## Jornada principal

1. O aluno se autentica no Spring Security.
2. Escolhe um tópico e um quiz ativo.
3. Inicia uma tentativa e responde questões de escolha única.
4. Abre um chat associado à questão daquela tentativa.
5. O servidor monta o contexto com tópico, instruções do quiz, enunciado, alternativas e mensagens anteriores; o navegador não envia um contexto arbitrário.
6. O tutor responde via OpenRouter e a aplicação persiste mensagens e progresso.

O estilo será descontraído e inspirado no modo de explicar do professor Felipe Cannarrozo Lourenço. O produto não se apresenta como o professor nem inventa citações ou falas atribuídas a ele. Exemplos concretos de aula só serão incorporados com autorização.

## Domínio persistente

O baseline inicial mapeia nove entidades: `AppUser`, `Topic`, `Quiz`, `QuizQuestion`, `QuizQuestionOption`, `QuizAttempt`, `QuestionAttempt`, `Conversation` e `Message`. As tabelas pertencem ao schema PostgreSQL `public` e são criadas por migrations Flyway. Uma tentativa tem progresso individual, cada questão respondida pertence ao mesmo quiz da tentativa e cada tentativa de questão possui no máximo uma conversa.

As regras de acesso serão aplicadas pelos serviços com a identidade da sessão. O usuário não pode consultar tentativa ou conversa de outro aluno. O gabarito permanece no backend e a explicação de uma resposta correta depende de política a confirmar pelo grupo.

## Arquitetura prevista

Aplicação monolítica Spring Boot com páginas Thymeleaf e API REST. Controllers recebem requests e DTOs; Services aplicam autorização, validação, montagem do contexto e transações; repositories Spring Data JPA persistem o domínio. OpenRouter é acessado por adapter no backend. A chamada externa ocorre fora de uma transação longa; mensagens idempotentes guardam estado de processamento e resultado.

PostgreSQL será hospedado no Supabase. O schema é aplicado por Flyway; a conexão depende de variáveis locais/de ambiente. O acesso ao Supabase e as credenciais OpenRouter serão fornecidos depois. Nenhum comando de migration será executado no banco do grupo sem acesso e revisão.

## Entrega inicial e próximos módulos

O baseline inclui estrutura Maven/Spring Boot, nove entidades e a migration única V1. Os incrementos seguintes implementam autenticação, catálogo/CRUD, tentativas e correção, chat contextualizado, adapter OpenRouter, telas, testes, OpenAPI e publicação.

## Critérios de aceite da jornada

- O aluno autenticado lista tópicos e quizzes ativos e inicia uma tentativa.
- Alternativas são corrigidas no backend; a resposta selecionada pertence à questão da tentativa.
- O chat é isolado por questão e por tentativa e recupera o histórico salvo.
- O servidor monta o contexto da LLM com dados persistidos e nunca expõe a chave de serviço ao browser.
- Uma falha de OpenRouter não apaga a pergunta nem o histórico anterior.
- Migrations Flyway e mappings JPA permanecem sincronizados no schema `public`.
- Nenhum acesso cruzado entre alunos é permitido.
