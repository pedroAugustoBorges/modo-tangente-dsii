# Registro de entregas de Thiago G. Soares

A interface do aluno usa a sessão e o layout Thymeleaf compartilhados. Após entrar, o aluno acessa o catálogo ou retoma uma tentativa em **Minhas tentativas**.

## Entregas

- `ead3603`, `e311096` e `29b5bf7` criaram a estrutura Thymeleaf inicial de estudo, catálogo e questão.
- `d9890ef` ligou a navegação aos tópicos e quizzes publicados.
- `be91d15` integrou início e retomada de tentativa, resposta, correção e progresso.
- `3d7acf5` integrou chat por questão, histórico e reenvio com o mesmo identificador para a mesma mensagem.
- Os testes de integração cobrem login, navegação, CSRF, isolamento de tentativas, resposta e recuperação de falha do tutor.

A validação local usa H2 e tutor simulado. A jornada com conteúdo definitivo, Supabase e OpenRouter deve ser validada na hospedagem compartilhada.
