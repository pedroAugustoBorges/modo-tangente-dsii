package br.edu.fatec.modotangente.repository;

import br.edu.fatec.modotangente.domain.QuestionAttempt;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, Long> {
    boolean existsByQuestionId(Long questionId);

    @Query("""
            select questionAttempt
            from QuestionAttempt questionAttempt
            join fetch questionAttempt.question question
            where questionAttempt.quizAttemptId = :quizAttemptId
            order by question.position
            """)
    List<QuestionAttempt> findAllForAttemptOrdered(@Param("quizAttemptId") Long quizAttemptId);

    Optional<QuestionAttempt> findByQuizAttemptIdAndQuestionId(Long quizAttemptId, Long questionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select questionAttempt
            from QuestionAttempt questionAttempt
            where questionAttempt.quizAttemptId = :quizAttemptId
              and questionAttempt.questionId = :questionId
            """)
    Optional<QuestionAttempt> findForUpdate(
            @Param("quizAttemptId") Long quizAttemptId,
            @Param("questionId") Long questionId);
}
