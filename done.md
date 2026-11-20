# Entregue nesta etapa — Modo Tangente

## Trabalho concluído

- Inspecionado o repositório e confirmada a ausência anterior de código Java, entidades JPA e migrations.
- Atualizado README e escopo para o fluxo tópico → quiz de múltipla escolha → tentativa → conversa por questão, usando Thymeleaf e Supabase PostgreSQL.
- Criado o baseline de nove entidades JPA e a migration única Flyway V1, com integridade referencial, schema `public`, índice case-insensitive de e-mail e checks de consistência.
- Adicionada API autenticada para iniciar uma tentativa de quiz ativo, listar tentativas próprias e consultar uma tentativa própria.
- A identidade do endpoint vem do principal; `userId` não é parâmetro HTTP. Tentativa alheia retorna 404 e conta desativada retorna 401.
- Atualizados os planos gerais e individuais dos quatro integrantes, preservando sete commits por integrante e ownership sem duplicação de entidade/migration.
- DBA e tech lead revisaram o modelo e as fronteiras de integração; os ajustes apontados foram incorporados.

## Verificações executadas

- `mvnw.cmd test`: 1 teste de contexto e 4 testes de controller passaram com Spring Boot 3.5.16, Java 17 e H2 isolado.
- O tech lead revisou P2 sem apontar bloqueio e confirmou a divisão entre Pedro, Enzo e João.
- Os planos Markdown/Word passaram na validação estrutural; cada integrante contém sete incrementos e o SQL propõe nove tabelas.

## Commits desta etapa

- `6147630` — `feat: cria baseline JPA e migration inicial integrada` (Pedro Augusto de Melo Borges)
- Segundo incremento de Pedro: API de tentativas, ownership, bloqueio de conta inativa e testes.

## Ainda pendente para o grupo

- Enzo deve integrar o login real com `AppUser`; o contrato exige `Authentication.getName()` como e-mail normalizado.
- João deve implementar catálogo, CRUD e `QuizContextProvider` sobre as entidades já existentes.
- Thiago deve implementar o adapter OpenRouter e as páginas de quiz/conversa sobre os endpoints compartilhados.
- O grupo precisa fornecer usuário/senha de um PostgreSQL de teste isolado e conteúdo de quizzes; a migration deve ser validada em PostgreSQL antes de usar o Supabase do grupo.
- Ainda falta decidir quando o tutor pode revelar o gabarito, configurar OpenRouter e destino de publicação.

## Limites da verificação

O teste local usa H2 e não executa a migration PostgreSQL V1. Nenhuma conexão ou migration foi realizada no Supabase. O login real e o fluxo ponta a ponta com OpenRouter ainda não estão prontos.
