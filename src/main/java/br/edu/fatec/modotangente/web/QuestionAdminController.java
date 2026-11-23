package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.QuestionCatalogService;
import br.edu.fatec.modotangente.web.dto.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/admin/quizzes/{quizId}/questions")
public class QuestionAdminController {
    private final QuestionCatalogService catalog;

    public QuestionAdminController(QuestionCatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public List<QuestionView> list(@PathVariable Long quizId) { return catalog.list(quizId); }

    @GetMapping("/{id}")
    public QuestionView get(@PathVariable Long quizId, @PathVariable Long id) { return catalog.get(quizId, id); }

    @PostMapping
    @ResponseStatus(CREATED)
    public QuestionView create(@PathVariable Long quizId, @Valid @RequestBody QuestionData data) {
        return catalog.save(quizId, null, data);
    }

    @PutMapping("/{id}")
    public QuestionView update(@PathVariable Long quizId, @PathVariable Long id, @Valid @RequestBody QuestionData data) {
        return catalog.save(quizId, id, data);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deactivate(@PathVariable Long quizId, @PathVariable Long id) {
        catalog.deactivate(quizId, id);
    }
}
