package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.CurrentAppUserService;
import br.edu.fatec.modotangente.service.StudentCatalogService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("/estudo")
public class StudyCatalogController {

    private final CurrentAppUserService users;
    private final StudentCatalogService catalog;

    public StudyCatalogController(CurrentAppUserService users, StudentCatalogService catalog) {
        this.users = users;
        this.catalog = catalog;
    }

    @GetMapping
    public String topics(Authentication authentication, Model model) {
        users.requireUserId(authentication);
        model.addAttribute("titulo", "Tópicos de estudo");
        model.addAttribute("mensagemVazia", "Nenhum tópico disponível.");
        model.addAttribute("itens", catalog.topics().stream()
                .map(topic -> new CatalogItem("/estudo/topicos/" + topic.id(), topic.title()))
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
