package br.edu.fatec.modotangente.repository;

import br.edu.fatec.modotangente.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByActiveTrueOrderByDisplayOrderAscIdAsc();
    Optional<Topic> findByIdAndActiveTrue(Long id);
    List<Topic> findAllByOrderByDisplayOrderAscIdAsc();
}
