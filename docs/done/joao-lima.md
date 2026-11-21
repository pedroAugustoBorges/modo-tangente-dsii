# Registro de entregas — João Lima

Atualizado em 21/11/2026. Registro construído a partir do histórico atualmente incorporado em `main`; não substitui o plano individual.

## Situação observada

Até esta data, não há commit de autoria do João incorporado ao histórico `main` consultado. Isso registra apenas o estado do repositório e não avalia trabalho local ou ainda não integrado.

## Contratos necessários para a integração

- Catálogo de tópicos, quizzes, questões e alternativas usando as entidades e tabelas já existentes.
- Implementação de produção do `QuizQuestionProvider.listActiveForAttempt(quizId)`, retornando referências `{questionId, position}`; o P3 do Pedro usa o contrato e mocka o provedor nos testes.
- `QuizContextProvider` para obter, do banco, tópico, quiz, enunciado e alternativas pertinentes ao tutor do P6.
- Preservação das questões já usadas por tentativas: desativar para novos quizzes, manter a consulta histórica e evitar editar conteúdo em lugar.

O provedor real é uma dependência para iniciar tentativas com dados reais. Os testes atuais do Pedro validam o contrato com H2 e mock, não substituem essa integração.
