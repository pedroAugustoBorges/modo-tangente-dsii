package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.repository.TopicRepository;
import br.edu.fatec.modotangente.web.dto.TopicView;
import br.edu.fatec.modotangente.service.CurrentAppUserService;
import org.springframework.security.core.Authentication;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/topics")
public class TopicController {
    private final TopicRepository topics;

    private final CurrentAppUserService users;

    public TopicController(TopicRepository topics, CurrentAppUserService users) {
        this.users = users;
        this.topics = topics;
    }

    @GetMapping
    public List<TopicView> list(Authentication authentication) {
        users.requireUserId(authentication);
        return topics.findByActiveTrueOrderByDisplayOrderAscIdAsc().stream().map(TopicView::from).toList();
    }

    @GetMapping("/{id}")
    public TopicView get(@PathVariable Long id, Authentication authentication) {
        users.requireUserId(authentication);
        return topics.findByIdAndActiveTrue(id).map(TopicView::from)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }
}
