package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.ConversationService;
import br.edu.fatec.modotangente.service.CurrentAppUserService;
import br.edu.fatec.modotangente.web.dto.ConversationSummary;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/attempts/{attemptId}/questions/{questionId}/conversation")
public class ConversationController {

    private final CurrentAppUserService currentAppUserService;
    private final ConversationService conversationService;

    public ConversationController(
            CurrentAppUserService currentAppUserService,
            ConversationService conversationService) {
        this.currentAppUserService = currentAppUserService;
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<ConversationSummary> getOrCreate(
            @PathVariable @Positive Long attemptId,
            @PathVariable @Positive Long questionId,
            Authentication authentication) {
        var result = conversationService.getOrCreate(
                currentAppUserService.requireUserId(authentication), attemptId, questionId);
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(result.conversation());
    }

    @GetMapping
    public ConversationSummary getExisting(
            @PathVariable @Positive Long attemptId,
            @PathVariable @Positive Long questionId,
            Authentication authentication) {
        return conversationService.findExisting(
                currentAppUserService.requireUserId(authentication), attemptId, questionId);
    }
}
