package br.edu.fatec.modotangente.repository;

import br.edu.fatec.modotangente.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    java.util.List<Topic> findByActiveTrueOrderByDisplayOrderAscIdAsc();
    java.util.Optional<Topic> findByIdAndActiveTrue(Long id);
    java.util.List<Topic> findAllByOrderByDisplayOrderAscIdAsc();
}
