package br.edu.fatec.modotangente.repository;

import br.edu.fatec.modotangente.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByTopicIdAndActiveTrueOrderByDisplayOrderAscIdAsc(Long topicId);
    Optional<Quiz> findByIdAndActiveTrueAndTopicActiveTrue(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from Quiz q where q.id = :id")
    Optional<Quiz> lockForCatalog(Long id);
    List<Quiz> findAllByOrderByDisplayOrderAscIdAsc();
    Optional<Quiz> findByIdAndActiveTrue(Long id);
}
