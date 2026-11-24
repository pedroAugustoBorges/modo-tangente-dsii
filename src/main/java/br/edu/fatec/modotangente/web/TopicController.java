package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.StudentCatalogService;
import br.edu.fatec.modotangente.web.dto.TopicView;
import br.edu.fatec.modotangente.service.CurrentAppUserService;
import org.springframework.security.core.Authentication;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topics")
public class TopicController {
    private final StudentCatalogService catalog;

    private final CurrentAppUserService users;

    public TopicController(StudentCatalogService catalog, CurrentAppUserService users) {
        this.users = users;
        this.catalog = catalog;
    }

    @GetMapping
    public List<TopicView> list(Authentication authentication) {
        users.requireUserId(authentication);
        return catalog.topics();
    }

    @GetMapping("/{id}")
    public TopicView get(@PathVariable Long id, Authentication authentication) {
        users.requireUserId(authentication);
        return catalog.topic(id);
    }
}
