package br.edu.fatec.modotangente.config;

import br.edu.fatec.modotangente.service.QuizQuestionProvider;
import br.edu.fatec.modotangente.service.StudentCatalogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogProviderConfiguration {
    @Bean
    QuizQuestionProvider quizQuestionProvider(StudentCatalogService catalog) {
        return catalog::listActiveForAttempt;
    }
}
