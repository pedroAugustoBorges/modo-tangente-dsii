package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.QuestionCatalogService;
import br.edu.fatec.modotangente.service.QuizCatalogService;
import br.edu.fatec.modotangente.web.dto.OptionData;
import br.edu.fatec.modotangente.web.dto.QuestionData;
import br.edu.fatec.modotangente.web.dto.QuestionView;
import br.edu.fatec.modotangente.web.dto.QuizView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/quizzes/{quizId}/questoes")
public class AdminQuestionPagesController {

    private final QuizCatalogService quizzes;
    private final QuestionCatalogService questions;

    public AdminQuestionPagesController(QuizCatalogService quizzes, QuestionCatalogService questions) {
        this.quizzes = quizzes;
        this.questions = questions;
    }

    @InitBinder("questao")
    void bindQuestionForm(WebDataBinder binder, HttpServletRequest request) {
        binder.setDeclarativeBinding(true);
        // The form submits consecutive option indices, with multiple fields per option.
        binder.setAutoGrowCollectionLimit(Math.max(binder.getAutoGrowCollectionLimit(), request.getParameterMap().size()));
    }

    @GetMapping
    public String list(@PathVariable Long quizId, Model model) {
        model.addAttribute("quiz", quizzes.get(quizId));
        model.addAttribute("questoes", questions.list(quizId));
        return "admin/questoes";
    }

    @GetMapping({"/nova", "/{questionId}"})
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public String edit(@PathVariable Long quizId, @PathVariable(required = false) Long questionId, Model model) {
        QuizView quiz = quizzes.get(quizId);
        QuestionView current = questionId == null ? null : questions.get(quizId, questionId);
        QuestionData form;
        if (current == null) {
            int highestPosition = questions.list(quizId).stream().filter(QuestionView::active)
                    .mapToInt(QuestionView::position).max().orElse(0);
            Integer nextPosition = highestPosition < Integer.MAX_VALUE ? highestPosition + 1 : null;
            form = new QuestionData(nextPosition, null, null, BigDecimal.ONE, false, List.of());
        } else {
            List<OptionData> options = questions.listOptions(quizId, questionId).stream()
                    .map(option -> new OptionData(option.position(), option.text(), option.correct()))
                    .toList();
            form = new QuestionData(current.position(), current.statement(), current.studyGuidance(), current.points(),
                    current.active(), options);
        }
        model.addAttribute("questao", form);
        return questionForm(quiz, current, form.options(), model);
    }

    @PostMapping({"/nova", "/{questionId}"})
    public String save(@PathVariable Long quizId, @PathVariable(required = false) Long questionId,
                       @Valid @ModelAttribute("questao") QuestionData form, BindingResult errors,
                       Model model, HttpServletResponse response, RedirectAttributes redirect) {
        int status = HttpStatus.BAD_REQUEST.value();
        if (!errors.hasErrors()) {
            try {
                QuestionView saved = questions.save(quizId, questionId, form);
                boolean newVersion = questionId != null && !questionId.equals(saved.id());
                redirect.addFlashAttribute("mensagem", newVersion
                        ? "Nova versão salva. As tentativas anteriores mantêm a questão original."
                        : "Questão salva.");
                return "redirect:" + listPath(quizId) + "/" + saved.id();
            } catch (ResponseStatusException exception) {
                if (exception.getStatusCode() != HttpStatus.BAD_REQUEST
                        && exception.getStatusCode() != HttpStatus.CONFLICT) {
                    throw exception;
                }
                status = exception.getStatusCode().value();
                errors.reject("catalog.question.invalid", exception.getReason());
            }
        }
        response.setStatus(status);
        model.addAttribute("camposInvalidos", errors.getFieldErrors().stream().map(FieldError::getField)
                .collect(Collectors.toSet()));
        return questionForm(quizzes.get(quizId), questionId == null ? null : questions.get(quizId, questionId),
                submittedOptions(form, errors), model);
    }

    @PostMapping("/{questionId}/desativar")
    public String deactivate(@PathVariable Long quizId, @PathVariable Long questionId, RedirectAttributes redirect) {
        questions.deactivate(quizId, questionId);
        redirect.addFlashAttribute("mensagem", "Questão despublicada.");
        return "redirect:" + listPath(quizId);
    }

    private String questionForm(QuizView quiz, QuestionView current, List<OptionData> options, Model model) {
        model.addAttribute("quiz", quiz);
        model.addAttribute("editada", current);
        model.addAttribute("alternativas", options);
        return "admin/questao";
    }

    private List<OptionData> submittedOptions(QuestionData form, BindingResult errors) {
        if (form != null && form.options() != null) {
            return form.options();
        }
        // Spring records the submitted option fields even when a number cannot be converted.
        if (!(errors.getFieldValue("options") instanceof List<?> options)) {
            return List.of();
        }
        return IntStream.range(0, options.size()).mapToObj(index -> {
            String field = "options[" + index + "].";
            Object position = errors.getFieldValue(field + "position");
            return new OptionData(position instanceof Integer number ? number : null,
                    (String) errors.getFieldValue(field + "text"),
                    Boolean.TRUE.equals(errors.getFieldValue(field + "correct")));
        }).toList();
    }

    private String listPath(Long quizId) {
        return "/admin/quizzes/" + quizId + "/questoes";
    }
}
