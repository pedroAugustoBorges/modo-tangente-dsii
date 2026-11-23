package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.domain.Quiz;
import br.edu.fatec.modotangente.repository.*;
import br.edu.fatec.modotangente.web.dto.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class QuizCatalogService {
    private final QuizRepository repository;
    private final TopicRepository topics;

    public QuizCatalogService(QuizRepository repository, TopicRepository topics) {
        this.repository = repository;
        this.topics = topics;
    }

    public List<QuizView> list() {
        return repository.findAllByOrderByDisplayOrderAscIdAsc().stream().map(QuizView::from).toList();
    }

    public QuizView get(Long id) {
        return QuizView.from(require(id));
    }

    private Quiz require(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @Transactional
    public QuizView save(Long id, QuizData data) {
        if (!topics.existsById(data.topicId())) {
            throw new ResponseStatusException(NOT_FOUND, "Tópico não encontrado.");
        }
        Quiz value = id == null ? new Quiz() : require(id);
        value.setTopicId(data.topicId());
        value.setTitle(data.title());
        value.setDescription(data.description());
        value.setInstructions(data.instructions());
        value.setDisplayOrder(data.displayOrder());
        value.setActive(data.active());
        return QuizView.from(repository.save(value));
    }

    @Transactional
    public void deactivate(Long id) {
        require(id).setActive(false);
    }
}
