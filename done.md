# Entregas por integrante — Modo Tangente

Este índice acompanha o que já foi incorporado ao repositório, por responsável. Os planos detalhados continuam fora do repositório. Atualizado em 22/11/2026.

| Integrante | Situação registrada |
|---|---|
| Pedro Augusto de Melo Borges | P1–P7 registrados em [done-pedro-augusto.md](docs/done/pedro-augusto.md); P3–P7 estão em commits locais à frente de `origin/main`, e P6/P7 passaram na suíte local. |
| Enzo Akira | Commit de escopo e README registrado em [done-enzo-akira.md](docs/done/enzo-akira.md); login, estrutura comum de interface e administração permanecem pendentes de integração. |
| João Lima | [done-joao-lima.md](docs/done/joao-lima.md) registra a situação observada no histórico; catálogo, CRUD e provedores de contexto continuam dependências de integração. |
| Thiago G. Soares | Três commits adicionaram a estrutura visual inicial; [done-thiago-soares.md](docs/done/thiago-soares.md) registra o que já está no repositório e as integrações ainda pendentes. |

## Próximo marco de integração

O P3 salva uma `QuestionAttempt` por questão usando o contrato `QuizQuestionProvider`; os testes substituem esse provedor por um mock até João entregar o bean de produção. Pedro já implementou o adaptador OpenRouter e os testes do contrato `QuizContextProvider`. A validação do fluxo com dados reais depende da integração dos provedores de catálogo. A suíte usa servidor HTTP simulado, sem acessar OpenRouter.
