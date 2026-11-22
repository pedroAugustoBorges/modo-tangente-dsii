package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.ConversationMessagePersistenceService;
import br.edu.fatec.modotangente.service.ConversationMessageService;
import br.edu.fatec.modotangente.service.CurrentAppUserService;
import br.edu.fatec.modotangente.service.MessageSubmissionResult;
import br.edu.fatec.modotangente.web.dto.MessageExchangeResponse;
import br.edu.fatec.modotangente.web.dto.MessageView;
import br.edu.fatec.modotangente.web.dto.SendMessageRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
public class ConversationMessageController {

    private final CurrentAppUserService currentAppUserService;
    private final ConversationMessagePersistenceService persistenceService;
    private final ConversationMessageService messageService;

    public ConversationMessageController(
            CurrentAppUserService currentAppUserService,
            ConversationMessagePersistenceService persistenceService,
            ConversationMessageService messageService) {
        this.currentAppUserService = currentAppUserService;
        this.persistenceService = persistenceService;
        this.messageService = messageService;
    }

    @GetMapping
    public List<MessageView> list(
            @PathVariable @Positive Long conversationId,
            Authentication authentication) {
        return persistenceService.listForOwner(
                currentAppUserService.requireUserId(authentication), conversationId);
    }

    @PostMapping
    public ResponseEntity<MessageExchangeResponse> send(
            @PathVariable @Positive Long conversationId,
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {
        MessageSubmissionResult result = messageService.send(
                currentAppUserService.requireUserId(authentication), conversationId, request);
        return ResponseEntity.status(result.status()).body(result.body());
    }
}
