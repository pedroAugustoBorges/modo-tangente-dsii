package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.domain.Conversation;
import br.edu.fatec.modotangente.domain.QuestionAttempt;
import br.edu.fatec.modotangente.repository.ConversationRepository;
import br.edu.fatec.modotangente.repository.QuestionAttemptRepository;
import br.edu.fatec.modotangente.repository.QuizAttemptRepository;
import br.edu.fatec.modotangente.web.dto.ConversationSummary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConversationService {

    private final QuizAttemptRepository attemptRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final ConversationRepository conversationRepository;

    public ConversationService(
            QuizAttemptRepository attemptRepository,
            QuestionAttemptRepository questionAttemptRepository,
            ConversationRepository conversationRepository) {
        this.attemptRepository = attemptRepository;
        this.questionAttemptRepository = questionAttemptRepository;
        this.conversationRepository = conversationRepository;
    }

    @Transactional
    public ConversationAccessResult getOrCreate(Long userId, Long attemptId, Long questionId) {
        requireOwnedAttempt(userId, attemptId);
        QuestionAttempt questionAttempt = questionAttemptRepository.findForUpdate(attemptId, questionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Questão não encontrada nesta tentativa."));

        return conversationRepository.findByQuestionAttemptId(questionAttempt.getId())
                .map(conversation -> new ConversationAccessResult(ConversationSummary.from(conversation), false))
                .orElseGet(() -> createFor(questionAttempt));
    }

    @Transactional(readOnly = true)
    public ConversationSummary findExisting(Long userId, Long attemptId, Long questionId) {
        requireOwnedAttempt(userId, attemptId);
        QuestionAttempt questionAttempt = questionAttemptRepository
                .findByQuizAttemptIdAndQuestionId(attemptId, questionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Questão não encontrada nesta tentativa."));

        return conversationRepository.findByQuestionAttemptId(questionAttempt.getId())
                .map(ConversationSummary::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa ainda não criada."));
    }

    private void requireOwnedAttempt(Long userId, Long attemptId) {
        if (attemptRepository.findByIdAndUserId(attemptId, userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tentativa não encontrada.");
        }
    }

    private ConversationAccessResult createFor(QuestionAttempt questionAttempt) {
        Conversation conversation = new Conversation();
        conversation.setQuestionAttemptId(questionAttempt.getId());
        Conversation savedConversation = conversationRepository.save(conversation);
        return new ConversationAccessResult(ConversationSummary.from(savedConversation), true);
    }
}
