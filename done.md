# Entregas por integrante — Modo Tangente

Este índice registra as entregas por integrante. Os planos individuais permanecem fora do repositório.

| Integrante | Entregas e situação atual |
|---|---|
| Pedro Augusto de Melo Borges | P1–P7 estão em `main`; o backend transmite ao tutor o resultado da resposta. A chamada real ao OpenRouter foi validada sem telas, via H2 e fluxo HTTP. [Registro detalhado](docs/done/pedro-augusto.md). |
| Enzo Akira | Login/logout com Spring Security, navegação compartilhada, painel administrativo integrado ao catálogo e preparação do pacote de deploy. [Registro detalhado](docs/done/enzo-akira.md). |
| João Lima | Catálogo didático, CRUD administrativo, questões versionadas, `QuizQuestionProvider`, `QuizContextProvider`, importação reproduzível e documentação integrados pelo PR #2 (`0fb1aaa`). [Registro detalhado](docs/done/joao-lima.md). |
| Thiago G. Soares | Interface Thymeleaf integrada a catálogo, tentativas, respostas, chat e histórico. Validação local com testes de integração. [Registro detalhado](docs/done/thiago-soares.md). |

## Próximo marco de integração

O próximo marco é carregar o conteúdo restante, publicar a aplicação e validar a administração do catálogo e a jornada de estudo no ambiente do grupo.

A suíte normal usa H2 e simula as chamadas ao OpenRouter. O fluxo HTTP do tutor já foi executado separadamente com o OpenRouter real e H2. A validação ponta a ponta com o Supabase compartilhado permanece pendente.
