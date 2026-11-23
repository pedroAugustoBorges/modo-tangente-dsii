# Registro de entregas — Pedro Augusto de Melo Borges

Atualizado em 22/11/2026. Este arquivo registra entregas e validações do Pedro; P6 e P7 estão em commits locais ainda não publicados no GitHub. O plano detalhado permanece fora do repositório.

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

## P5 — Mensagens e histórico (entregue neste commit)

- Criadas as rotas `GET /api/conversations/{conversationId}/messages` e `POST /api/conversations/{conversationId}/messages`.
- O histórico é ordenado por `createdAt` e `id`; leitura e envio verificam a propriedade da conversa pela tentativa e pelo usuário autenticado.
- O POST recebe somente `content` e `requestId`, salva a mensagem do aluno como `PROCESSING`, pede a resposta pelo contrato backend `TutorService` e persiste a resposta `TUTOR` antes de marcar a mensagem do aluno como `COMPLETED`.
- Repetir o mesmo ID e conteúdo retorna a resposta persistida sem chamar o tutor novamente; reutilizar o ID com conteúdo diferente retorna 409. Pedido já em andamento retorna 202. Falha do tutor marca `FAILED` e permite retry com o mesmo requestId.
- A migration V1 já continha as colunas, constraints e índice necessários; não foi criada migration adicional.
- Validação local do P5: `mvnw.cmd -q -ntp test`, 13 testes aprovados; OpenAPI parseado como YAML.
- O tutor é substituído por mock nos testes. Este incremento não chama OpenRouter nem monta o contexto didático completo.

## P6 — TutorService e adaptador OpenRouter

- Implementado `OpenRouterTutorService`, com Chat Completions, configuração externa e chave exclusivamente no backend. Modelo padrão e limites são configuráveis por ambiente.
- Criado o contrato `QuizContextProvider.loadForQuestion(questionId)` e o DTO `QuizTutorContext`, alinhado à issue mais recente do João. O serviço deriva `questionId` da conversa persistida e passa ao provedor tópico, quiz, questão, instruções e alternativas sem campo de gabarito; a implementação de produção pertence ao catálogo do João.
- O prompt orienta o tutor a usar tom descontraído inspirado no professor Felipe Cannarrozo Lourenço, sem se passar por ele ou inventar falas. Também orienta a não revelar o gabarito.
- O histórico enviado inclui até oito trocas completas anteriores e a mensagem atual, respeita o limite configurável de caracteres e preserva sempre contexto e pergunta atuais.
- Cliente HTTP tem timeouts configuráveis. Status de erro, falha de transporte, JSON inválido e conteúdo ausente são convertidos em exceções internas sanitizadas; chave e corpo remoto não são expostos.
- Adicionada migration Flyway V2 com `processing_started_at`, incluindo preenchimento de registros antigos em `PROCESSING`. Após dois minutos sem atividade (configurável), uma repetição do mesmo `requestId` pode recuperar a mensagem. O lock da conversa serializa essa reivindicação.
- `.env.example`, propriedades e README documentam a configuração; `.env` permanece ignorado pelo Git.

## P7 — Testes integrados e documentação

- Adicionados testes sem rede para mapeamento de papéis/contexto, limite de histórico, modelo e tokens configurados, resposta válida/vazia, JSON inválido, HTTP 401/429/503, timeout, chave ausente e orçamento de contexto.
- Teste de controller cobre recuperação de mensagem antiga em `PROCESSING`; os demais cobrem ownership, resposta, idempotência, retries e falha.
- Atualizados `docs/openapi.yaml`, `docs/escopo.md`, README e este registro.
- Validação local: `mvnw.cmd -q -ntp test`, **24 testes aprovados**. Testes usam H2 e OpenRouter simulado; nenhuma chamada real foi feita pela suíte e a V2 não foi aplicada ao Supabase do grupo.

## Integração pendente do grupo

- João ainda precisa implementar os beans de produção `QuizQuestionProvider` e `QuizContextProvider`; até lá, o início real de tentativas e a resolução do contexto dependem de mocks nos testes.
- Enzo integra autenticação real sobre `AppUser`; Thiago conecta as telas ao contrato de mensagens.
- A configuração OpenRouter passou por uma prova separada de conectividade; isso não substitui a integração com catálogo real.

## Integrações necessárias para a entrega do grupo

- João: bean de produção `QuizQuestionProvider` e catálogo; depois, `QuizContextProvider` usado pelo tutor.
- Enzo: autenticação real sobre `AppUser` e estrutura comum de navegação/layout.
- Thiago: telas Thymeleaf do estudante integradas aos contratos de tentativa, questão, conversa e mensagens.
- Pedro: mantém o domínio de tentativas, conversas, mensagens e serviço de tutor/OpenRouter.
- Até João integrar `QuizQuestionProvider` e `QuizContextProvider`, o perfil normal não inicia o fluxo completo; os testes substituem esses contratos por mocks.
