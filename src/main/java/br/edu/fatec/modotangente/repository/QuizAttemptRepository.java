package br.edu.fatec.modotangente.repository;

import br.edu.fatec.modotangente.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findAllByUserIdOrderByStartedAtDesc(Long userId);
    Optional<QuizAttempt> findByIdAndUserId(Long id, Long userId);
}
