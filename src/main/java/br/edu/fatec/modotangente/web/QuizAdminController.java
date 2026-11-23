package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.QuizCatalogService;
import br.edu.fatec.modotangente.web.dto.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/admin/quizzes")
public class QuizAdminController {
    private final QuizCatalogService catalog;

    public QuizAdminController(QuizCatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public List<QuizView> list() { return catalog.list(); }

    @GetMapping("/{id}")
    public QuizView get(@PathVariable Long id) { return catalog.get(id); }

    @PostMapping
    @ResponseStatus(CREATED)
    public QuizView create(@Valid @RequestBody QuizData data) {
        return catalog.save(null, data);
    }

    @PutMapping("/{id}")
    public QuizView update(@PathVariable Long id, @Valid @RequestBody QuizData data) {
        return catalog.save(id, data);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deactivate(@PathVariable Long id) { catalog.deactivate(id); }
}
