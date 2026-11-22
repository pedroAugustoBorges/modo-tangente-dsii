# Registro de entregas — Pedro Augusto de Melo Borges

Atualizado em 21/11/2026. Este arquivo registra entregas incorporadas pelo Pedro e o próximo trabalho da sua trilha; o plano detalhado permanece fora do repositório.

## Entregue

- **P1 — Baseline da aplicação:** entidades JPA e migration Flyway V1 compartilhadas, projeto Spring Boot compilável e escopo/documentação alinhados. Commit `6147630`.
- **P2 — Tentativas autenticadas:** início de tentativa em quiz ativo, listagem filtrada pelo usuário da sessão e consulta protegida de tentativa própria. Commit `0e73366`.
- **P3 — Progresso e resposta por questão:** ao iniciar uma tentativa, cria `QuestionAttempt` para cada referência de questão retornada pelo `QuizQuestionProvider`, na mesma transação; `GET /api/attempts/{attemptId}` retorna progresso ordenado; `POST /api/attempts/{attemptId}/questions/{questionId}/answer` valida a alternativa no backend, calcula o resultado e protege contra alteração de resposta depois do primeiro envio. Inclui o contrato OpenAPI e os testes desta etapa. Commit `f099946` após rebase sobre as mudanças do Thiago.

## Decisão de integração do P3

O serviço do Pedro consome `QuizQuestionProvider.listActiveForAttempt(quizId)`, com referências `{questionId, position}`. Neste incremento, os testes fornecem um mock para esse contrato. A implementação real deve ser entregue pelo responsável pelo catálogo, João, sem duplicar o bean. Até essa integração, o teste H2 valida o contrato e o serviço de tentativas, mas o perfil normal da aplicação não inicia: falta o bean de produção exigido pelo `QuizAttemptService`.

## Verificação do P3

- `mvnw.cmd -q -ntp test`: 7 testes passaram (1 teste de contexto e 6 testes de controller), sem falhas, erros ou ignorados.
- Os testes usam H2; não constituem validação do Supabase. A conexão Supabase e a migration V1 foram verificadas anteriormente, conforme registrado no histórico do projeto.
- A suíte cobre criação/listagem sob o usuário autenticado, acesso negado a tentativa alheia, progresso ordenado, resposta e repetição idempotente da mesma alternativa, conflito para alternativa diferente e rejeição de alternativa pertencente a outra questão.

## P4 — Conversa por questão (entregue neste commit)

- Criada a rota `POST /api/attempts/{attemptId}/questions/{questionId}/conversation`: retorna 201 ao criar e 200 ao recuperar a conversa única já existente.
- Criada a rota GET correspondente para recuperar uma conversa criada; GET retorna 404 antes da criação.
- O serviço verifica o proprietário da tentativa e a questão pertencente a ela. O lock na `QuestionAttempt` serializa solicitações simultâneas de criação; a restrição única persistente continua assegurando uma conversa por questão/tentativa.
- Nenhuma conversa é criada ao iniciar o quiz. A mesma questão em outra tentativa recebe outra conversa.
- Contrato OpenAPI atualizado. Testes cobrem criação lazy, POST repetido, GET, separação entre tentativas e 404 para conteúdo de outro aluno.
- Validação local do P4: `mvnw.cmd -q -ntp test`, 9 testes aprovados; YAML do OpenAPI parseado com sucesso.

## Depois do P4

- **P5:** persistir e listar mensagens em ordem estável; registrar mensagens do aluno e respostas do tutor com `requestId`, status de processamento e comportamento de retry idempotente.
- **P6:** implementar o serviço de contexto e o adaptador OpenRouter no backend, integrar o contexto persistido do catálogo, tratar timeouts/erros/respostas inválidas e manter credenciais fora do Git e do navegador.
- **P7:** fechar testes de integração e segurança entre tentativas, conversas, mensagens, ownership e tutor; revisar contratos OpenAPI e instruções de execução.

## Integrações necessárias para a entrega do grupo

- João: bean de produção `QuizQuestionProvider` e catálogo; depois, `QuizContextProvider` usado pelo tutor.
- Enzo: autenticação real sobre `AppUser` e estrutura comum de navegação/layout.
- Thiago: telas Thymeleaf do estudante integradas aos contratos de tentativa, questão, conversa e mensagens.
- Pedro: mantém o domínio de tentativas, conversas, mensagens e serviço de tutor/OpenRouter.
