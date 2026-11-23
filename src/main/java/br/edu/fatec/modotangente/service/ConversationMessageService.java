package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.web.dto.MessageExchangeResponse;
import br.edu.fatec.modotangente.web.dto.SendMessageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConversationMessageService {

    private final ConversationMessagePersistenceService persistenceService;
    private final TutorService tutorService;

    public ConversationMessageService(
            ConversationMessagePersistenceService persistenceService,
            TutorService tutorService) {
        this.persistenceService = persistenceService;
        this.tutorService = tutorService;
    }

    public MessageSubmissionResult send(Long userId, Long conversationId, SendMessageRequest request) {
        ConversationMessagePersistenceService.SubmissionClaim claim = persistenceService.begin(
                userId, conversationId, request.content(), request.requestId());

        if (claim.state() == ConversationMessagePersistenceService.ClaimState.COMPLETED) {
            return new MessageSubmissionResult(HttpStatus.OK, claim.response());
        }
        if (claim.state() == ConversationMessagePersistenceService.ClaimState.PROCESSING) {
            return new MessageSubmissionResult(HttpStatus.ACCEPTED, claim.response());
        }

        String reply;
        try {
            reply = tutorService.generateReply(claim.questionAttemptId(), claim.history());
            if (reply == null || reply.isBlank()) {
                throw new IllegalStateException("O tutor retornou uma resposta vazia.");
            }
        } catch (RuntimeException exception) {
            persistenceService.markFailed(claim.studentMessageId());
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Não foi possível concluir a resposta do tutor. A mensagem pode ser reenviada.");
        }

        MessageExchangeResponse response = persistenceService.complete(claim.studentMessageId(), reply);
        return new MessageSubmissionResult(claim.created() ? HttpStatus.CREATED : HttpStatus.OK, response);
    }
}
