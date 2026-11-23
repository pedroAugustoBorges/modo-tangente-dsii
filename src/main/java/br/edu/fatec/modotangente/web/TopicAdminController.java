package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.TopicCatalogService;
import br.edu.fatec.modotangente.web.dto.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/admin/topics")
public class TopicAdminController {
    private final TopicCatalogService catalog;

    public TopicAdminController(TopicCatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public List<TopicView> list() { return catalog.list(); }

    @GetMapping("/{id}")
    public TopicView get(@PathVariable Long id) { return catalog.get(id); }

    @PostMapping
    @ResponseStatus(CREATED)
    public TopicView create(@Valid @RequestBody TopicData data) {
        return catalog.save(null, data);
    }

    @PutMapping("/{id}")
    public TopicView update(@PathVariable Long id, @Valid @RequestBody TopicData data) {
        return catalog.save(id, data);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deactivate(@PathVariable Long id) { catalog.deactivate(id); }
}
