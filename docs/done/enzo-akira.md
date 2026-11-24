# Registro de entregas — Enzo Akira

Atualizado em 24/11/2026.

## Entrega registrada

- Commit `6e0a0bc` — escopo inicial do projeto e atualização de integrante no README, incluindo documentação de escopo em `docs/escopo.md`.
- Commit `2299d87` — login BCrypt sobre `AppUser`, logout, identidade da sessão e autorização de administração.
- Commit `497e301` — layout e navegação Thymeleaf compartilhados com os templates do Thiago.
- Preparação do deploy — pacote executável, verificação de saúde e instruções no README. As migrations V1 e V2 foram aplicadas em PostgreSQL isolado, com validação do esquema pelo Hibernate.
- Painel Thymeleaf em `/admin` — formulários de tópicos, quizzes, questões e alternativas ligados aos serviços do João. A edição de questões usadas em tentativas segue o versionamento do catálogo.
- Cobertura das rotas reais do painel — acesso por perfil, CSRF, validação dos formulários e preservação das questões de tentativas anteriores.

## Validação do painel

- `clean verify` gerou o JAR executável com 57 testes aprovados. O teste com chamada real ao OpenRouter é opcional e ficou fora desta execução.
- Em PostgreSQL 17 isolado, o Flyway aplicou V1 a V4, o Hibernate validou o esquema e a verificação de saúde retornou `UP`. No navegador, foram criados um tópico, um quiz e uma questão publicada com duas alternativas.

## Pendências para a entrega do grupo

- Definir a hospedagem, publicar a aplicação e validar o fluxo completo com o Supabase compartilhado e o OpenRouter.
