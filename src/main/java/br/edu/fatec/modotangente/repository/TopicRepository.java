package br.edu.fatec.modotangente.repository;

import br.edu.fatec.modotangente.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {
}
