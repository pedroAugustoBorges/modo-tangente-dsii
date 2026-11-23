package br.edu.fatec.modotangente.repository;

import br.edu.fatec.modotangente.domain.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuizIdAndActiveTrueOrderByPositionAsc(Long quizId);
    Optional<QuizQuestion> findByQuizIdAndPositionAndActiveTrue(Long quizId, Integer position);
    List<QuizQuestion> findByQuizIdOrderByPositionAscIdAsc(Long quizId);
    Optional<QuizQuestion> findByIdAndQuizId(Long id, Long quizId);
}
