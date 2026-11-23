package br.edu.fatec.modotangente.repository;

import br.edu.fatec.modotangente.domain.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    java.util.List<QuizQuestion> findByQuizIdOrderByPositionAscIdAsc(Long quizId);
    java.util.Optional<QuizQuestion> findByIdAndQuizId(Long id, Long quizId);
}
