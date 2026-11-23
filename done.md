# Entregas por integrante — Modo Tangente

Este índice registra o que está incorporado em `main` e as validações locais de 23/11/2026. As mudanças deste teste ainda estão locais e não commitadas; os planos individuais permanecem fora do repositório.

| Integrante | Entregas e situação atual |
|---|---|
| Pedro Augusto de Melo Borges | P1–P7 estão em `main`; a extensão local transmite ao tutor o resultado da resposta. A chamada real ao OpenRouter foi validada sem telas, via H2 e fluxo HTTP. [Registro detalhado](docs/done/pedro-augusto.md). |
| Enzo Akira | Login/logout com Spring Security, layout e navegação compartilhados e preparação do pacote de deploy. A integração das páginas administrativas e do fluxo de estudo permanece. [Registro detalhado](docs/done/enzo-akira.md). |
| João Lima | Catálogo didático, CRUD administrativo, questões versionadas, `QuizQuestionProvider`, `QuizContextProvider`, importação reproduzível e documentação integrados pelo PR #2 (`0fb1aaa`). [Registro detalhado](docs/done/joao-lima.md). |
| Thiago G. Soares | Templates Thymeleaf iniciais de catálogo e questão. Controllers/model, ações de resposta e integração do chat com as APIs continuam pendentes. [Registro detalhado](docs/done/thiago-soares.md). |

## Próximo marco de integração

Os provedores de catálogo já estão implementados. A próxima entrega do grupo é conectar as telas Thymeleaf aos endpoints reais de catálogo, tentativa, resposta e conversa, e validar o fluxo visual de login até a resposta do tutor.

Na suíte normal, 43 testes passaram e o teste live opt-in foi ignorado, sem falhas ou erros. O fluxo HTTP sem interface foi executado separadamente com credencial do `.env`: o OpenRouter respondeu após a tentativa ser corrigida e vinculada à conversa. A validação usou H2; o Supabase compartilhado ainda não foi exercitado nesta etapa.
