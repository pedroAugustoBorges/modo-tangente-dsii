package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.QuizCatalogService;
import br.edu.fatec.modotangente.service.TopicCatalogService;
import br.edu.fatec.modotangente.web.dto.QuizData;
import br.edu.fatec.modotangente.web.dto.QuizView;
import br.edu.fatec.modotangente.web.dto.TopicData;
import br.edu.fatec.modotangente.web.dto.TopicView;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminCatalogPagesController {

    private final TopicCatalogService topics;
    private final QuizCatalogService quizzes;

    public AdminCatalogPagesController(TopicCatalogService topics, QuizCatalogService quizzes) {
        this.topics = topics;
        this.quizzes = quizzes;
    }

    @InitBinder({"topico", "quiz"})
    void bindCatalogForms(WebDataBinder binder) {
        binder.setDeclarativeBinding(true);
    }

    @GetMapping
    public String index(Model model) {
        var catalogTopics = topics.list();
        model.addAttribute("topicos", catalogTopics);
        model.addAttribute("quizzes", quizzes.list());
        model.addAttribute("titulosDosTopicos", catalogTopics.stream()
                .collect(Collectors.toMap(TopicView::id, TopicView::title)));
        return "admin/catalogo";
    }

    @GetMapping({"/topicos/novo", "/topicos/{topicId}"})
    public String editTopic(@PathVariable(required = false) Long topicId, Model model) {
        TopicView current = topicId == null ? null : topics.get(topicId);
        TopicData form = current == null ? new TopicData(null, null, 0, true)
                : new TopicData(current.title(), current.description(), current.displayOrder(), current.active());
        model.addAttribute("topico", form);
        return topicForm(current, model);
    }

    @PostMapping({"/topicos/novo", "/topicos/{topicId}"})
    public String saveTopic(@PathVariable(required = false) Long topicId,
                            @Valid @ModelAttribute("topico") TopicData form, BindingResult errors,
                            Model model, HttpServletResponse response, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return topicForm(topicId == null ? null : topics.get(topicId), model);
        }
        topics.save(topicId, form);
        redirect.addFlashAttribute("mensagem", "Tópico salvo.");
        return "redirect:/admin";
    }

    @PostMapping("/topicos/{topicId}/desativar")
    public String deactivateTopic(@PathVariable Long topicId, RedirectAttributes redirect) {
        topics.deactivate(topicId);
        redirect.addFlashAttribute("mensagem", "Tópico desativado.");
        return "redirect:/admin";
    }

    @GetMapping({"/quizzes/novo", "/quizzes/{quizId}"})
    public String editQuiz(@PathVariable(required = false) Long quizId, Model model) {
        QuizView current = quizId == null ? null : quizzes.get(quizId);
        QuizData form = current == null ? new QuizData(null, null, null, null, 0, false)
                : new QuizData(current.topicId(), current.title(), current.description(), current.instructions(),
                        current.displayOrder(), current.active());
        model.addAttribute("quiz", form);
        return quizForm(current, model);
    }

    @PostMapping({"/quizzes/novo", "/quizzes/{quizId}"})
    public String saveQuiz(@PathVariable(required = false) Long quizId,
                           @Valid @ModelAttribute("quiz") QuizData form, BindingResult errors,
                           Model model, HttpServletResponse response, RedirectAttributes redirect) {
        QuizView current = quizId == null ? null : quizzes.get(quizId);
        if (!errors.hasErrors()) {
            try {
                quizzes.save(quizId, form);
                redirect.addFlashAttribute("mensagem", "Quiz salvo.");
                return "redirect:/admin";
            } catch (ResponseStatusException exception) {
                if (exception.getStatusCode() != HttpStatus.NOT_FOUND || exception.getReason() == null) {
                    throw exception;
                }
                errors.rejectValue("topicId", "catalog.topic.notFound", "Tópico não encontrado.");
            }
        }
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return quizForm(current, model);
    }

    @PostMapping("/quizzes/{quizId}/desativar")
    public String deactivateQuiz(@PathVariable Long quizId, RedirectAttributes redirect) {
        quizzes.deactivate(quizId);
        redirect.addFlashAttribute("mensagem", "Quiz desativado.");
        return "redirect:/admin";
    }

    private String topicForm(TopicView current, Model model) {
        model.addAttribute("editado", current);
        return "admin/topico";
    }

    private String quizForm(QuizView current, Model model) {
        model.addAttribute("editado", current);
        model.addAttribute("topicos", topics.list());
        return "admin/quiz";
    }
}
