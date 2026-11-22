package br.edu.fatec.modotangente.repository;

import br.edu.fatec.modotangente.domain.Message;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByConversationIdOrderByCreatedAtAscIdAsc(Long conversationId);

    Optional<Message> findByConversationIdAndRequestId(Long conversationId, UUID requestId);

    Optional<Message> findByReplyToMessageId(Long replyToMessageId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select message from Message message where message.id = :messageId")
    Optional<Message> findForUpdateById(@Param("messageId") Long messageId);
}
