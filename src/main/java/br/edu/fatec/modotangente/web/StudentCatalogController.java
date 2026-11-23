package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.*;
import br.edu.fatec.modotangente.web.dto.*;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class StudentCatalogController {
    private final CurrentAppUserService users;
    private final StudentCatalogService catalog;

    public StudentCatalogController(CurrentAppUserService users, StudentCatalogService catalog) {
        this.users = users;
        this.catalog = catalog;
    }

    @GetMapping("/topics/{topicId}/quizzes")
    public List<QuizView> quizzes(@PathVariable Long topicId, Authentication authentication) {
        users.requireUserId(authentication);
        return catalog.quizzes(topicId);
    }

    @GetMapping("/quizzes/{quizId}/questions")
    public List<StudentQuestion> questions(@PathVariable Long quizId, Authentication authentication) {
        users.requireUserId(authentication);
        return catalog.questions(quizId);
    }

    @GetMapping("/attempts/{attemptId}/questions/{questionId}")
    public StudentQuestion historical(@PathVariable Long attemptId, @PathVariable Long questionId,
                                      Authentication authentication) {
        return catalog.historical(users.requireUserId(authentication), attemptId, questionId);
    }
}
