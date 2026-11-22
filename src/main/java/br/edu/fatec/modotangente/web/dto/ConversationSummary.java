package br.edu.fatec.modotangente.web.dto;

import br.edu.fatec.modotangente.domain.Conversation;

import java.time.Instant;

public record ConversationSummary(
        Long id,
        Long questionAttemptId,
        Instant createdAt,
        Instant updatedAt) {

    public static ConversationSummary from(Conversation conversation) {
        return new ConversationSummary(
                conversation.getId(),
                conversation.getQuestionAttemptId(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt());
    }
}
