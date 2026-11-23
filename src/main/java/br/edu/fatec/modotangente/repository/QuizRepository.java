package br.edu.fatec.modotangente.repository;

import br.edu.fatec.modotangente.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select q from Quiz q where q.id = :id")
    Optional<Quiz> lockForCatalog(Long id);
    java.util.List<Quiz> findAllByOrderByDisplayOrderAscIdAsc();
    Optional<Quiz> findByIdAndActiveTrue(Long id);
}
