# Catálogo inicial de quizzes DS-II

Arquivo importável: `src/main/resources/catalog/dsii-fundamentos-v1.json`.

## Escopo

O catálogo contém cinco tópicos — POO em Java, Anotações, Reflection, Spring Boot e JPA — com um quiz de cinco questões de múltipla escolha por tópico. Cada questão tem quatro alternativas, uma resposta correta e uma orientação de estudo para apoiar a explicação do tutor. O nível é introdutório/intermediário e o conteúdo acompanha os conceitos usados na disciplina de Desenvolvimento de Servidores II.

O material é um primeiro conjunto para revisão, não um banco completo nem uma representação de falas do professor. O conteúdo das perguntas permanece neutro; o tom do tutor é responsabilidade do prompt de sistema da integração de IA. As questões JPA seguem Jakarta Persistence 3.1, versão gerenciada pelo Spring Boot 3.5.16 neste projeto, e as questões de Spring Boot são compatíveis com a linha 3.5 usada pelo projeto.

## Fontes técnicas oficiais

- Java: [Classes e objetos](https://dev.java/learn/language/oop/classes-objects/), [herança](https://dev.java/learn/inheritance/) e [polimorfismo](https://dev.java/learn/language/oop/inheritance/polymorphism/).
- Java SE 17: [@Retention](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/annotation/Retention.html), [RetentionPolicy](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/annotation/RetentionPolicy.html), [@Inherited](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/annotation/Inherited.html), e [Class](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Class.html) para `getMethods()` / `getDeclaredMethods()`.
- Spring Boot 3.5: [@SpringBootApplication](https://docs.spring.io/spring-boot/3.5/reference/using/using-the-springbootapplication-annotation.html) e [configuração externa](https://docs.spring.io/spring-boot/3.5/reference/features/external-config.html). Spring Framework 6.2: [injeção por construtor](https://docs.spring.io/spring-framework/reference/6.2/core/beans/annotation-config/autowired.html).
- Jakarta Persistence 3.1: [especificação](https://jakarta.ee/specifications/persistence/3.1/jakarta-persistence-spec-3.1), incluindo estados de entidade, `merge`, `@ManyToOne` e associações bidirecionais. A versão `3.1.0` é a [dependência gerenciada pelo Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/appendix/dependency-versions/coordinates.html).

## Importação

Configure `CATALOG_IMPORT_RESOURCE=classpath:catalog/dsii-fundamentos-v1.json` para carregar este conjunto na inicialização. O importador existente valida o lote, executa a gravação em transação e registra `source` mais SHA-256 para impedir duplicação acidental. Não execute o importador contra um banco compartilhado sem escolher conscientemente o ambiente; este arquivo não contém credenciais nem acessa o Supabase por conta própria.
