# Registro de entregas — Pedro Augusto de Melo Borges

Atualizado em 24/11/2026. P1–P7 e a integração complementar abaixo estão incorporados em `main`/`origin/main` após o commit desta atualização. O plano detalhado permanece fora do repositório.

## Entregue

- **P1 — Baseline da aplicação:** entidades JPA e migration Flyway V1 compartilhadas, projeto Spring Boot compilável e escopo/documentação alinhados. Commit `6147630`.
- **P2 — Tentativas autenticadas:** início de tentativa em quiz ativo, listagem filtrada pelo usuário da sessão e consulta protegida de tentativa própria. Commit `0e73366`.
- **P3 — Progresso e resposta por questão:** ao iniciar uma tentativa, cria `QuestionAttempt` para cada referência de questão retornada pelo `QuizQuestionProvider`, na mesma transação; `GET /api/attempts/{attemptId}` retorna progresso ordenado; `POST /api/attempts/{attemptId}/questions/{questionId}/answer` valida a alternativa no backend, calcula o resultado e protege contra alteração de resposta depois do primeiro envio. Inclui o contrato OpenAPI e os testes desta etapa. Commit `f099946` após rebase sobre as mudanças do Thiago.

## Decisão de integração do P3

O serviço do Pedro consome `QuizQuestionProvider.listActiveForAttempt(quizId)`, com referências `{questionId, position}`. Nos testes iniciais do P3, um mock substituía esse contrato enquanto o catálogo não estava pronto. João implementou o provedor de produção no catálogo integrado pelo PR #2 (`0fb1aaa`); o bean exigido pelo `QuizAttemptService` já está presente em `main`. A suíte ainda usa mocks nos testes isolados de controllers, e os testes de catálogo cobrem os provedores reais.

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
- O contrato `QuizContextProvider.loadForQuestionAttempt(questionAttemptId)` e o DTO `QuizTutorContext` carregam o tópico, quiz, questão, instruções, alternativas e resposta persistida daquela tentativa. O adapter recebe a alternativa selecionada e o resultado calculado pelo backend; os demais marcadores de correção não entram no prompt. O provedor de catálogo foi incorporado pelo merge `0fb1aaa`; sua extensão para resolver o estado da tentativa está nesta atualização local.
- O prompt usa tom descontraído inspirado no professor Felipe Cannarrozo Lourenço, sem se passar por ele nem inventar falas. Quando o aluno pergunta se acertou, pode confirmar usando o resultado do backend; se errou, deve orientar sem indicar diretamente a alternativa correta.
- O histórico enviado inclui até oito trocas completas anteriores e a mensagem atual, respeita o limite configurável de caracteres e preserva sempre contexto e pergunta atuais.
- Cliente HTTP tem timeouts configuráveis. Status de erro, falha de transporte, JSON inválido e conteúdo ausente são convertidos em exceções internas sanitizadas; chave e corpo remoto não são expostos.
- Adicionada migration Flyway V2 com `processing_started_at`, incluindo preenchimento de registros antigos em `PROCESSING`. Após dois minutos sem atividade (configurável), uma repetição do mesmo `requestId` pode recuperar a mensagem. O lock da conversa serializa essa reivindicação.
- `.env.example`, propriedades e README documentam a configuração; `.env` permanece ignorado pelo Git.

## P7 — Testes integrados e documentação

- Adicionados testes sem rede para mapeamento de papéis/contexto, limite de histórico, modelo e tokens configurados, resposta válida/vazia, JSON inválido, HTTP 401/429/503, timeout, chave ausente e orçamento de contexto.
- Teste de controller cobre recuperação de mensagem antiga em `PROCESSING`; os demais cobrem ownership, resposta, idempotência, retries e falha.
- Atualizados `docs/openapi.yaml`, `docs/escopo.md`, README e este registro.
- Suíte normal em 23/11: `mvnw.cmd -q -ntp test` detectou 44 testes; 43 passaram e o teste live opt-in foi ignorado, sem falhas ou erros.
- Adicionado teste de integração em `CatalogIntegrationTests`: o provedor real lê dados e resultado da resposta do H2; o adaptador envia questão, alternativa selecionada, resultado e pergunta do aluno a um HTTP simulado, sem enviar o gabarito das demais alternativas.
- Adicionado `OpenRouterConversationLiveTest`, opt-in por `OPENROUTER_LIVE_TEST=true`: simula via MockMvc as rotas de iniciar tentativa, responder à questão, criar conversa e enviar mensagem. Foi executado com H2 e credencial carregada do `.env`; a chamada real ao OpenRouter concluiu e retornou resposta não vazia.
- Na execução normal da suíte, o teste live fica ignorado por padrão; o teste de integração local não usa rede. A execução live não acessou o Supabase e não aplicou migrations no banco compartilhado.
- Complemento de 24/11: o catálogo agora apresenta Spring Boot e Spring Web MVC como tópicos separados; `spring-web-v1.json` acrescenta cinco questões sem alterar a origem nem o checksum da base v1. `CATALOG_IMPORT_RESOURCES` carrega base e complemento na mesma inicialização, preservando suporte ao `CATALOG_IMPORT_RESOURCE` legado.
- O teste de integração importa ambos os arquivos, confirma idempotência e consulta o quiz Spring Web pelas rotas REST, sem expor gabarito. O teste de configuração verifica a ordem e o suporte aos dois formatos de propriedade. A inicialização Spring Boot com os dois recursos em H2 também foi verificada.
- A suíte normal em 24/11 detectou 50 testes: 49 passaram e o live opt-in foi ignorado, sem falhas nem erros. A chamada real foi executada separadamente, com a chave do `.env` carregada apenas no processo do teste, sem imprimir a chave; a resposta ao aluno confirmou que acertou e explicou a injeção de dependências. Não houve acesso ao Supabase.
- O adapter agora registra apenas o status HTTP ou a classe da falha de transporte quando o OpenRouter falha; corpo remoto e credenciais continuam fora dos logs.

## Integração pendente do grupo

- João entregou o catálogo e os beans de produção `QuizQuestionProvider` e `QuizContextProvider`; o teste de integração atualizado cobre a ligação do contexto persistido ao adaptador OpenRouter.
- Enzo entregou autenticação e layout comum; a interface administrativa ainda precisa ser integrada às rotas de catálogo.
- Thiago entregou templates iniciais do estudante; ainda faltam controllers/model Thymeleaf e integração das telas com catálogo, tentativas, respostas e chat.
- Pedro completou os sete incrementos do plano. A chamada real ao OpenRouter foi validada pelo fluxo HTTP sem interface, usando H2; o fluxo visual e o Supabase compartilhado ainda não foram validados de ponta a ponta.

## Integrações necessárias para a entrega do grupo

- João: catálogo e provedores de produção entregues; falta a integração das telas administrativas e de estudo com a API.
- Enzo: autenticação real sobre `AppUser` e estrutura comum de navegação/layout entregues; a integração das páginas administrativas permanece.
- Thiago: estrutura inicial Thymeleaf entregue; falta integrar as telas do estudante às rotas de tentativa, questão, conversa e mensagens.
- Pedro: P1–P7 entregues; contexto por tentativa, resultado de correção e chamada OpenRouter validados em teste H2/MockMvc sem telas.
- O teste live comprovou uma resposta real da API externa; não comprova integração de interface nem execução sobre o Supabase compartilhado.
