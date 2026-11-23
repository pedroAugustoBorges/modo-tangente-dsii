# Registro de entregas — João Lima

Atualizado em 23/11/2026. A branch `codex/catalogo-didatico` reúne sete incrementos do catálogo didático sobre a base `aaa7c5f`.

## Entregas registradas

- **Tópicos** — leitura dos tópicos ativos por ordem de exibição e CRUD administrativo com DTOs. Commit `60aaa3b`.
- **Quizzes** — criação, consulta, edição e desativação. O serviço valida a existência do tópico antes de salvar o vínculo. Commit `ad68157`.
- **Questões** — gestão de enunciado, posição, orientação e pontuação. Editar uma questão usada em tentativa cria uma versão. A migration V3 permite que a versão histórica preserve sua posição. Commit `9b78578`.
- **Alternativas e publicação** — questão e alternativas são salvas na mesma transação. Publicar exige posições distintas, pelo menos duas alternativas e exatamente uma correta. Commit `da4f8b4`.
- **Catálogo do aluno e tentativas** — leitura do conteúdo ativo, alternativas sem marcador de correção e consulta da versão histórica pelo dono da tentativa. O contrato `QuizQuestionProvider.listActiveForAttempt(quizId)` fornece as referências `{questionId, position}` em ordem. Commit `5554f8d`.
- **Contexto do tutor** — `QuizContextProvider.loadForQuestion(questionId)` deriva do banco o tópico, o quiz, o enunciado, a orientação e os textos das alternativas em ordem. Commit `78bb6be`.
- **Carga inicial, testes e documentação** — importador transacional com registro de origem e SHA-256 na migration V4. Reexecutar o mesmo lote preserva os IDs. Inclui o exemplo de injeção de dependência, os testes de integração e os contratos da API. Commit `43ef0d5`. [OpenAPI](../openapi.yaml)

## Verificação

- `mvnw.cmd verify` concluiu com 42 testes aprovados e gerou o JAR executável. A suíte cobre autorização e CSRF, vínculos inválidos, publicação, ordenação, preservação de histórico, contexto do tutor e reexecução da carga.
- Em PostgreSQL 16.16 isolado, o Flyway aplicou V1 a V4 no schema `public`, o Hibernate validou o mapeamento e os cinco testes de integração do catálogo executados nesse banco passaram.
- O cenário específico de H2 verifica que a atualização de uma base local antiga preserva as questões históricas e permite reutilizar a posição.

## Pendências para a entrega do grupo

- Integrar as telas de estudo e administração às rotas do catálogo.
- Registrar a confirmação de João e Pedro sobre a assinatura do provedor já exercitada pelo serviço de tentativas.
- Importar as demais questões quando o grupo disponibilizar o material com sua origem.
