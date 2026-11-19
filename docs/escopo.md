# Escopo inicial do Modo Tangente

Proposta de primeira entrega para o grupo de Desenvolvimento de Servidores II. [README do projeto](../README.md)

## Objetivo

Ajudar estudantes da disciplina a revisar conteúdos pelo chat. O tutor responde dúvidas em português, com explicações diretas e exemplos curtos de código quando ajudarem a entender o assunto. Os temas previstos incluem Java, orientação a objetos, Reflection, Servlets, Spring, Spring Boot e JPA.

## Primeira entrega

- **Tirar dúvidas pelo chat.** O estudante inicia uma conversa, envia uma pergunta e recebe uma resposta gerada via OpenRouter.
- **Continuar uma conversa.** O tutor usa as mensagens anteriores da mesma conversa para responder a perguntas de acompanhamento. O estudante pode reabrir conversas salvas e continuar de onde parou.
- **Usar material da disciplina.** O grupo seleciona um material para a demonstração e o usa como referência para as respostas sobre o tema escolhido.
- **Lidar com falhas no serviço de respostas.** Se o OpenRouter estiver indisponível, o chat informa o problema e permite tentar de novo.

## Base técnica prevista

A interface web conversa com uma API REST em Java e Spring Boot. O backend consulta o modelo pelo OpenRouter e salva as conversas e mensagens em um banco H2 em arquivo, com Spring Data JPA e Hibernate.

## Critérios de aceite

Na demonstração da primeira entrega, o grupo deve conseguir verificar estes cenários.

| Cenário | Resultado esperado |
| --- | --- |
| Abrir uma conversa e perguntar o que é um Servlet | O chat exibe uma explicação em português com um exemplo curto em Java. |
| Perguntar na mesma conversa como adaptar o exemplo para receber um parâmetro da requisição | A resposta adapta o exemplo anterior. |
| Reiniciar a aplicação e abrir a conversa salva | O histórico aparece na ordem original e é possível enviar outra mensagem. |
| Fazer uma pergunta coberta pelo material selecionado | A explicação usa um conceito ou exemplo presente nesse material. |
| Simular uma falha no OpenRouter | O chat mostra uma mensagem de erro clara e permite reenviar a pergunta. |

## Decisões em aberto

- Escolher a tecnologia da interface web e o modelo no OpenRouter.
- Selecionar o material inicial e definir como ele vai chegar ao tutor.
- Definir como publicar a aplicação e disponibilizar a documentação Swagger/OpenAPI.
