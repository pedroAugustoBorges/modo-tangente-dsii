package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.CurrentAppUserService;
import br.edu.fatec.modotangente.service.QuizAttemptService;
import br.edu.fatec.modotangente.web.dto.QuizAttemptSummary;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class QuizAttemptController {

    private final CurrentAppUserService currentAppUserService;
    private final QuizAttemptService attemptService;

    public QuizAttemptController(CurrentAppUserService currentAppUserService, QuizAttemptService attemptService) {
        this.currentAppUserService = currentAppUserService;
        this.attemptService = attemptService;
    }

    @PostMapping("/quizzes/{quizId}/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizAttemptSummary start(@PathVariable @Positive Long quizId, Authentication authentication) {
        return attemptService.start(currentAppUserService.requireUserId(authentication), quizId);
    }

    @GetMapping("/attempts")
    public List<QuizAttemptSummary> list(Authentication authentication) {
        return attemptService.listForUser(currentAppUserService.requireUserId(authentication));
    }

    @GetMapping("/attempts/{attemptId}")
    public QuizAttemptSummary get(@PathVariable @Positive Long attemptId, Authentication authentication) {
        return attemptService.findOwned(currentAppUserService.requireUserId(authentication), attemptId);
    }
}
