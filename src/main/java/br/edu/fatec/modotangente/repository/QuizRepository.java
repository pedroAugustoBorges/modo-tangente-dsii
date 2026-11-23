package br.edu.fatec.modotangente.repository;

import br.edu.fatec.modotangente.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    java.util.List<Quiz> findAllByOrderByDisplayOrderAscIdAsc();
    Optional<Quiz> findByIdAndActiveTrue(Long id);
}
