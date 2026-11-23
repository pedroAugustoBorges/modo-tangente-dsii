package br.edu.fatec.modotangente.repository;

import br.edu.fatec.modotangente.domain.QuizQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface QuizQuestionOptionRepository extends JpaRepository<QuizQuestionOption, Long> {
    List<QuizQuestionOption> findByQuestionIdInOrderByPositionAsc(List<Long> questionIds);
    List<QuizQuestionOption> findByQuestionIdOrderByPositionAsc(Long questionId);
    Optional<QuizQuestionOption> findByIdAndQuestionId(Long id, Long questionId);
}
