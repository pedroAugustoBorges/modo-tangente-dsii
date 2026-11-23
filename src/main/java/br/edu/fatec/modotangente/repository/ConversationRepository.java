package br.edu.fatec.modotangente.repository;

import br.edu.fatec.modotangente.domain.Conversation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByQuestionAttemptId(Long questionAttemptId);

    @Query("select conversation.questionAttempt.questionId from Conversation conversation where conversation.id = :conversationId")
    Optional<Long> findQuestionIdByConversationId(@Param("conversationId") Long conversationId);

    @Query("""
            select conversation
            from Conversation conversation
            join conversation.questionAttempt questionAttempt
            join questionAttempt.quizAttempt quizAttempt
            where conversation.id = :conversationId
              and quizAttempt.userId = :userId
            """)
    Optional<Conversation> findOwnedById(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select conversation from Conversation conversation where conversation.id = :conversationId")
    Optional<Conversation> findForUpdateById(@Param("conversationId") Long conversationId);
}
