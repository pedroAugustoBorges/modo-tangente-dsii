package br.edu.fatec.modotangente.repository;

import br.edu.fatec.modotangente.domain.QuizQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizQuestionOptionRepository extends JpaRepository<QuizQuestionOption, Long> {
    Optional<QuizQuestionOption> findByIdAndQuestionId(Long id, Long questionId);
}
