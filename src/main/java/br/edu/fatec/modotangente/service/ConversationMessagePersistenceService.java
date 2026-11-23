package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.domain.Conversation;
import br.edu.fatec.modotangente.domain.Message;
import br.edu.fatec.modotangente.repository.ConversationRepository;
import br.edu.fatec.modotangente.repository.MessageRepository;
import br.edu.fatec.modotangente.web.dto.MessageExchangeResponse;
import br.edu.fatec.modotangente.web.dto.MessageView;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class ConversationMessagePersistenceService {

    private static final String STUDENT = "STUDENT";
    private static final String TUTOR = "TUTOR";
    private static final String PROCESSING = "PROCESSING";
    private static final String COMPLETED = "COMPLETED";
    private static final String FAILED = "FAILED";

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final Duration processingTimeout;

    public ConversationMessagePersistenceService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            @Value("${tutor.processing-timeout:PT2M}") Duration processingTimeout) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        if (processingTimeout.isZero() || processingTimeout.isNegative()) {
            throw new IllegalArgumentException("tutor.processing-timeout precisa ser positivo.");
        }
        this.processingTimeout = processingTimeout;
    }

    @Transactional(readOnly = true)
    public List<MessageView> listForOwner(Long userId, Long conversationId) {
        requireOwnedConversation(userId, conversationId);
        return messageRepository.findAllByConversationIdOrderByCreatedAtAscIdAsc(conversationId).stream()
                .map(MessageView::from)
                .toList();
    }

    @Transactional
    public SubmissionClaim begin(Long userId, Long conversationId, String content, UUID requestId) {
        Conversation conversation = requireOwnedConversation(userId, conversationId);
        conversationRepository.findForUpdateById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa não encontrada."));
        Long questionAttemptId = conversation.getQuestionAttemptId();

        Message studentMessage = messageRepository.findByConversationIdAndRequestId(conversationId, requestId)
                .orElse(null);
        boolean created = studentMessage == null;

        if (studentMessage != null && !studentMessage.getContent().equals(content)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "O requestId já foi usado com outro conteúdo nesta conversa.");
        }

        if (studentMessage != null && COMPLETED.equals(studentMessage.getLlmStatus())) {
            Message tutorMessage = messageRepository.findByReplyToMessageId(studentMessage.getId())
                    .orElseThrow(() -> new IllegalStateException("Resposta concluída sem mensagem do tutor."));
            return SubmissionClaim.completed(studentMessage, tutorMessage, questionAttemptId);
        }

        if (studentMessage != null && PROCESSING.equals(studentMessage.getLlmStatus())) {
            Instant startedAt = studentMessage.getProcessingStartedAt();
            boolean isFresh = startedAt != null && startedAt.isAfter(Instant.now().minus(processingTimeout));
            if (isFresh) {
                return SubmissionClaim.processing(studentMessage, questionAttemptId);
            }
            // A crashed process may leave a request stuck in PROCESSING. The conversation lock
            // ensures only one retry can claim it after the configured timeout.
            studentMessage.setLlmStatus(FAILED);
        }

        if (created) {
            studentMessage = new Message();
            studentMessage.setConversationId(conversationId);
            studentMessage.setSenderRole(STUDENT);
            studentMessage.setContent(content);
            studentMessage.setRequestId(requestId);
        }
        studentMessage.setLlmStatus(PROCESSING);
        studentMessage.setProcessingStartedAt(Instant.now());
        studentMessage = messageRepository.saveAndFlush(studentMessage);

        List<TutorService.TutorTurn> history = messageRepository
                .findAllByConversationIdOrderByCreatedAtAscIdAsc(conversationId).stream()
                .map(message -> new TutorService.TutorTurn(message.getSenderRole(), message.getContent()))
                .toList();

        return SubmissionClaim.claimed(studentMessage, questionAttemptId, history, created);
    }

    @Transactional
    public MessageExchangeResponse complete(Long studentMessageId, String reply) {
        Message studentMessage = messageRepository.findForUpdateById(studentMessageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada."));
        if (!PROCESSING.equals(studentMessage.getLlmStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A mensagem não está em processamento.");
        }

        Message tutorMessage = new Message();
        tutorMessage.setConversationId(studentMessage.getConversationId());
        tutorMessage.setSenderRole(TUTOR);
        tutorMessage.setContent(reply);
        tutorMessage.setReplyToMessageId(studentMessage.getId());
        tutorMessage = messageRepository.saveAndFlush(tutorMessage);

        studentMessage.setLlmStatus(COMPLETED);
        studentMessage.setProcessingStartedAt(null);
        studentMessage = messageRepository.saveAndFlush(studentMessage);
        return new MessageExchangeResponse(MessageView.from(studentMessage), MessageView.from(tutorMessage));
    }

    @Transactional
    public void markFailed(Long studentMessageId) {
        messageRepository.findForUpdateById(studentMessageId).ifPresent(studentMessage -> {
            if (PROCESSING.equals(studentMessage.getLlmStatus())) {
                studentMessage.setLlmStatus(FAILED);
                studentMessage.setProcessingStartedAt(null);
                messageRepository.save(studentMessage);
            }
        });
    }

    private Conversation requireOwnedConversation(Long userId, Long conversationId) {
        return conversationRepository.findOwnedById(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa não encontrada."));
    }

    public record SubmissionClaim(
            ClaimState state,
            boolean created,
            Long questionAttemptId,
            Long studentMessageId,
            MessageExchangeResponse response,
            List<TutorService.TutorTurn> history) {

        static SubmissionClaim completed(Message studentMessage, Message tutorMessage, Long questionAttemptId) {
            return new SubmissionClaim(
                    ClaimState.COMPLETED,
                    false,
                    questionAttemptId,
                    studentMessage.getId(),
                    new MessageExchangeResponse(MessageView.from(studentMessage), MessageView.from(tutorMessage)),
                    List.of());
        }

        static SubmissionClaim processing(Message studentMessage, Long questionAttemptId) {
            return new SubmissionClaim(
                    ClaimState.PROCESSING,
                    false,
                    questionAttemptId,
                    studentMessage.getId(),
                    new MessageExchangeResponse(MessageView.from(studentMessage), null),
                    List.of());
        }

        static SubmissionClaim claimed(
                Message studentMessage,
                Long questionAttemptId,
                List<TutorService.TutorTurn> history,
                boolean created) {
            return new SubmissionClaim(
                    ClaimState.CLAIMED,
                    created,
                    questionAttemptId,
                    studentMessage.getId(),
                    new MessageExchangeResponse(MessageView.from(studentMessage), null),
                    history);
        }
    }

    public enum ClaimState {
        CLAIMED,
        PROCESSING,
        COMPLETED
    }
}
