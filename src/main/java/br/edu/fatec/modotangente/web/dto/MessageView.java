package br.edu.fatec.modotangente.web.dto;

import br.edu.fatec.modotangente.domain.Message;

import java.time.Instant;
import java.util.UUID;

public record MessageView(
        Long id,
        String senderRole,
        String content,
        UUID requestId,
        Long replyToMessageId,
        String llmStatus,
        Instant createdAt) {

    public static MessageView from(Message message) {
        return new MessageView(
                message.getId(),
                message.getSenderRole(),
                message.getContent(),
                message.getRequestId(),
                message.getReplyToMessageId(),
                message.getLlmStatus(),
                message.getCreatedAt());
    }
}
