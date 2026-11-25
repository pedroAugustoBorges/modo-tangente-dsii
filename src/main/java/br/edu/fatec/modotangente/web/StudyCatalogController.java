package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.CurrentAppUserService;
import br.edu.fatec.modotangente.service.QuizAttemptService;
import br.edu.fatec.modotangente.service.StudentCatalogService;
import br.edu.fatec.modotangente.web.dto.QuizView;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("/estudo")
public class StudyCatalogController {

    private final CurrentAppUserService users;
    private final StudentCatalogService catalog;
    private final QuizAttemptService attempts;

    public StudyCatalogController(CurrentAppUserService users, StudentCatalogService catalog,
                                  QuizAttemptService attempts) {
        this.users = users;
        this.catalog = catalog;
        this.attempts = attempts;
    }

    @GetMapping
    public String topics(Authentication authentication, Model model) {
        users.requireUserId(authentication);
        model.addAttribute("titulo", "Tópicos de estudo");
        model.addAttribute("mensagemVazia", "Nenhum tópico disponível.");
        model.addAttribute("abrirTopico", true);
        model.addAttribute("itens", catalog.topics().stream()
                .map(topic -> new CatalogItem("/estudo/topicos/" + topic.id() + "/abrir", topic.title()))
                .toList());
        return "estudo/catalogo";
    }

    @GetMapping("/topicos/{topicId}")
    public String quizzes(@PathVariable Long topicId, Authentication authentication, Model model) {
        users.requireUserId(authentication);
        model.addAttribute("voltar", "/estudo");
        model.addAttribute("titulo", "Quizzes do tópico");
        model.addAttribute("mensagemVazia", "Nenhum quiz disponível neste tópico.");
        try {
            var topic = catalog.topic(topicId);
            List<CatalogItem> items = catalog.quizzes(topicId).stream()
                    .map(quiz -> new CatalogItem("/estudo/quizzes/" + quiz.id(), quiz.title()))
                    .toList();
            model.addAttribute("titulo", topic.title());
            model.addAttribute("itens", items);
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().value() != 404) {
                throw exception;
            }
            model.addAttribute("erro", "Este tópico não está disponível.");
        }
        return "estudo/catalogo";
    }

    @PostMapping("/topicos/{topicId}/abrir")
    public String openTopic(@PathVariable Long topicId, Authentication authentication) {
        Long userId = users.requireUserId(authentication);
        String topicPage = "redirect:/estudo/topicos/" + topicId;
        List<QuizView> quizzes;
        try {
            quizzes = catalog.quizzes(topicId);
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                return topicPage;
            }
            throw exception;
        }
        if (quizzes.size() != 1) {
            return topicPage;
        }

        Long quizId = quizzes.get(0).id();
        try {
            return "redirect:/estudo/tentativas/" + attempts.resumeOrStart(userId, topicId, quizId).id();
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode() == HttpStatus.CONFLICT) {
                return "redirect:/estudo/quizzes/" + quizId;
            }
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                return topicPage;
            }
            throw exception;
        }
    }

    @GetMapping("/quizzes/{quizId}")
    public String quiz(@PathVariable Long quizId, Authentication authentication, Model model) {
        users.requireUserId(authentication);
        var quiz = catalog.quiz(quizId);
        model.addAttribute("quiz", quiz);
        model.addAttribute("disponivel", !catalog.questions(quizId).isEmpty());
        return "estudo/quiz";
    }

    public record CatalogItem(String url, String titulo) { }
}
