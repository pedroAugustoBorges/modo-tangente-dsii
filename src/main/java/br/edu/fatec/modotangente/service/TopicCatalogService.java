package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.domain.Topic;
import br.edu.fatec.modotangente.repository.*;
import br.edu.fatec.modotangente.web.dto.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class TopicCatalogService {
    private final TopicRepository repository;

    public TopicCatalogService(TopicRepository repository) {
        this.repository = repository;
    }

    public List<TopicView> list() {
        return repository.findAllByOrderByDisplayOrderAscIdAsc().stream().map(TopicView::from).toList();
    }

    public TopicView get(Long id) {
        return TopicView.from(require(id));
    }

    private Topic require(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @Transactional
    public TopicView save(Long id, TopicData data) {
        Topic value = id == null ? new Topic() : require(id);
        value.setTitle(data.title());
        value.setDescription(data.description());
        value.setDisplayOrder(data.displayOrder());
        value.setActive(data.active());
        return TopicView.from(repository.save(value));
    }

    @Transactional
    public void deactivate(Long id) {
        require(id).setActive(false);
    }
}
