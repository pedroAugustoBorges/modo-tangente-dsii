package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.repository.TopicRepository;
import br.edu.fatec.modotangente.web.dto.TopicView;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/topics")
public class TopicController {
    private final TopicRepository topics;

    public TopicController(TopicRepository topics) {
        this.topics = topics;
    }

    @GetMapping
    public List<TopicView> list() {
        return topics.findByActiveTrueOrderByDisplayOrderAscIdAsc().stream().map(TopicView::from).toList();
    }

    @GetMapping("/{id}")
    public TopicView get(@PathVariable Long id) {
        return topics.findByIdAndActiveTrue(id).map(TopicView::from)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }
}
