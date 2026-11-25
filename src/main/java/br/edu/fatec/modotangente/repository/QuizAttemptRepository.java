package br.edu.fatec.modotangente.repository;

import br.edu.fatec.modotangente.domain.QuizAttempt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findAllByUserIdOrderByStartedAtDesc(Long userId);
    Optional<QuizAttempt> findByIdAndUserId(Long id, Long userId);

    @Query("""
            select attempt
            from QuizAttempt attempt
            where attempt.userId = :userId
              and attempt.quizId = :quizId
              and attempt.status = 'IN_PROGRESS'
              and exists (
                  select 1 from QuestionAttempt questionAttempt
                  where questionAttempt.quizAttemptId = attempt.id
                    and questionAttempt.answeredAt is null
              )
            order by attempt.startedAt desc, attempt.id desc
            """)
    List<QuizAttempt> findResumable(@Param("userId") Long userId, @Param("quizId") Long quizId, Pageable page);
}
