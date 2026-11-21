# Entregas por integrante — Modo Tangente

Este índice acompanha o que já foi incorporado ao repositório, por responsável. Os planos detalhados continuam fora do repositório. Atualizado em 21/11/2026.

| Integrante | Situação registrada |
|---|---|
| Pedro Augusto de Melo Borges | P1, P2 e P3 registrados em [done-pedro-augusto.md](docs/done/pedro-augusto.md). P4 começa após o commit do P3. |
| Enzo Akira | Commit de escopo e README registrado em [done-enzo-akira.md](docs/done/enzo-akira.md); login, estrutura comum de interface e administração permanecem pendentes de integração. |
| João Lima | [done-joao-lima.md](docs/done/joao-lima.md) registra a situação observada no histórico; catálogo, CRUD e provedores de contexto continuam dependências de integração. |
| Thiago G. Soares | [done-thiago-soares.md](docs/done/thiago-soares.md) registra a situação observada no histórico; telas do estudante continuam pendentes de integração. |

## Próximo marco de integração

O P3 salva uma `QuestionAttempt` por questão usando o contrato `QuizQuestionProvider`. Os testes substituem esse provedor por um mock. A implementação real do contrato pertence ao catálogo do João e precisa ser integrada antes de iniciar tentativas com dados reais. O P4 cria ou recupera uma conversa para uma questão da tentativa; não inclui ainda as mensagens nem a chamada ao OpenRouter.
