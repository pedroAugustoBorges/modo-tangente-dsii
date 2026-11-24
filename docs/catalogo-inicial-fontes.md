# Catálogo inicial de quizzes DS-II

Arquivos importáveis: `src/main/resources/catalog/dsii-fundamentos-v1.json` e `src/main/resources/catalog/spring-web-v1.json`.

## Escopo

O catálogo contém seis tópicos — POO em Java, Anotações, Reflection, Spring Boot, JPA e Spring Web (Spring MVC) — com um quiz de cinco questões de múltipla escolha por tópico. Cada questão tem quatro alternativas, uma resposta correta e uma orientação de estudo para apoiar a explicação do tutor. O nível é introdutório/intermediário e o conteúdo acompanha os conceitos usados na disciplina de Desenvolvimento de Servidores II. Spring Boot e Spring Web são tópicos distintos: Boot cobre a configuração e a execução da aplicação; Spring Web cobre controllers, mapeamento de rotas HTTP, leitura de corpos de requisição e tratamento de respostas/erros.

O arquivo v1 mantém os cinco tópicos inicialmente publicados. O arquivo `spring-web-v1.json` é um lote aditivo independente, com origem própria, para incluir o sexto tópico sem reaplicar nem duplicar os registros do arquivo anterior.

O material é um primeiro conjunto para revisão, não um banco completo nem uma representação de falas do professor. O conteúdo das perguntas permanece neutro; o tom do tutor é responsabilidade do prompt de sistema da integração de IA. As questões JPA seguem Jakarta Persistence 3.1, versão gerenciada pelo Spring Boot 3.5.16 neste projeto, e as questões de Spring Boot são compatíveis com a linha 3.5 usada pelo projeto.

## Fontes técnicas oficiais

- Java: [Classes e objetos](https://dev.java/learn/language/oop/classes-objects/), [herança](https://dev.java/learn/inheritance/) e [polimorfismo](https://dev.java/learn/language/oop/inheritance/polymorphism/).
- Java SE 17: [@Retention](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/annotation/Retention.html), [RetentionPolicy](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/annotation/RetentionPolicy.html), [@Inherited](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/annotation/Inherited.html), e [Class](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Class.html) para `getMethods()` / `getDeclaredMethods()`.
- Spring Boot 3.5: [@SpringBootApplication](https://docs.spring.io/spring-boot/3.5/reference/using/using-the-springbootapplication-annotation.html) e [configuração externa](https://docs.spring.io/spring-boot/3.5/reference/features/external-config.html). Spring Framework 6.2: [injeção por construtor](https://docs.spring.io/spring-framework/reference/6.2/core/beans/annotation-config/autowired.html).
- Spring Framework 6.2 / Spring Web MVC: [controllers anotados](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-controller.html), [mapeamento de requisições](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-controller/ann-requestmapping.html), [leitura de corpos com `@RequestBody`](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-controller/ann-methods/requestbody.html) e [tratamento de exceções](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-controller/ann-exceptionhandler.html).
- Jakarta Persistence 3.1: [especificação](https://jakarta.ee/specifications/persistence/3.1/jakarta-persistence-spec-3.1), incluindo estados de entidade, `merge`, `@ManyToOne` e associações bidirecionais. A versão `3.1.0` é a [dependência gerenciada pelo Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/appendix/dependency-versions/coordinates.html).

## Importação

Em banco novo, carregue `dsii-fundamentos-v1.json` na primeira inicialização e `spring-web-v1.json` na seguinte, alterando `CATALOG_IMPORT_RESOURCE` entre as execuções. Se a base v1 já foi importada, carregue apenas `spring-web-v1.json`. A propriedade aceita um arquivo por inicialização. O importador valida cada lote, grava em transação e registra `source` mais SHA-256 para impedir duplicação acidental. Remova a configuração de importação depois de incluir os lotes. Os arquivos não contêm credenciais nem acessam o Supabase por conta própria.
