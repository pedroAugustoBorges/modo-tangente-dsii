package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.CurrentAppUserService;
import br.edu.fatec.modotangente.service.QuizAttemptService;
import br.edu.fatec.modotangente.service.StudentCatalogService;
import br.edu.fatec.modotangente.web.dto.QuestionAttemptProgress;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/estudo")
public class StudyAttemptController {

    private final CurrentAppUserService users;
    private final QuizAttemptService attempts;
    private final StudentCatalogService catalog;

    public StudyAttemptController(CurrentAppUserService users, QuizAttemptService attempts,
                                  StudentCatalogService catalog) {
        this.users = users;
        this.attempts = attempts;
        this.catalog = catalog;
    }

    @GetMapping("/tentativas")
    public String history(Authentication authentication, Model model) {
        model.addAttribute("titulo", "Minhas tentativas");
        model.addAttribute("voltar", "/estudo");
        model.addAttribute("mensagemVazia", "Você ainda não iniciou um quiz.");
        model.addAttribute("itens", attempts.listForUser(users.requireUserId(authentication)).stream()
                .map(attempt -> new StudyCatalogController.CatalogItem(
                        "/estudo/tentativas/" + attempt.id(),
                        "Tentativa " + attempt.id() + " · Quiz " + attempt.quizId()))
                .toList());
        return "estudo/catalogo";
    }

    @PostMapping("/quizzes/{quizId}/tentativas")
    public String start(@PathVariable Long quizId, Authentication authentication, RedirectAttributes redirect) {
        Long userId = users.requireUserId(authentication);
        try {
            return "redirect:/estudo/tentativas/" + attempts.start(userId, quizId).id();
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode() == HttpStatus.CONFLICT) {
                redirect.addFlashAttribute("erro", "Este quiz ainda não tem questões publicadas.");
                return "redirect:/estudo/quizzes/" + quizId;
            }
            throw exception;
        }
    }

    @GetMapping("/tentativas/{attemptId}")
    public String resume(@PathVariable Long attemptId, Authentication authentication, Model model) {
        var attempt = attempts.findOwned(users.requireUserId(authentication), attemptId);
        var questions = attempt.questionAttempts();
        if (questions.isEmpty()) {
            model.addAttribute("titulo", "Tentativa " + attemptId);
            model.addAttribute("erro", "Esta tentativa não contém questões.");
            model.addAttribute("voltar", "/estudo/tentativas");
            return "estudo/catalogo";
        }
        var current = questions.stream().filter(question -> !question.answered())
                .findFirst().orElse(questions.get(questions.size() - 1));
        return "redirect:" + questionPath(attemptId, current.questionId());
    }

    @GetMapping("/tentativas/{attemptId}/questoes/{questionId}")
    public String question(@PathVariable Long attemptId, @PathVariable Long questionId,
                           Authentication authentication, Model model) {
        Long userId = users.requireUserId(authentication);
        var attempt = attempts.findOwned(userId, attemptId);
        List<QuestionAttemptProgress> questions = attempt.questionAttempts();
        var progress = questions.stream().filter(entry -> questionId.equals(entry.questionId()))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var question = catalog.historical(userId, attemptId, questionId);
        model.addAttribute("titulo", "Questão do quiz");
        model.addAttribute("tentativaId", attemptId);
        model.addAttribute("questao", question);
        model.addAttribute("progresso", progress);
        model.addAttribute("posicao", questions.indexOf(progress) + 1);
        model.addAttribute("questoes", questions);
        model.addAttribute("respondidas", questions.stream().filter(QuestionAttemptProgress::answered).count());
        model.addAttribute("editavel", !progress.answered() && "IN_PROGRESS".equals(attempt.status()));
        return "estudo/questao";
    }

    @PostMapping("/tentativas/{attemptId}/questoes/{questionId}/resposta")
    public String answer(@PathVariable Long attemptId, @PathVariable Long questionId,
                         @RequestParam(required = false) Long optionId,
                         Authentication authentication, RedirectAttributes redirect) {
        Long userId = users.requireUserId(authentication);
        catalog.historical(userId, attemptId, questionId);
        if (optionId == null) {
            redirect.addFlashAttribute("erro", "Selecione uma alternativa.");
        } else {
            try {
                attempts.answer(userId, attemptId, questionId, optionId);
            } catch (ResponseStatusException exception) {
                if (exception.getStatusCode() == HttpStatus.CONFLICT) {
                    redirect.addFlashAttribute("erro", "Esta questão não aceita mais respostas.");
                } else if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                    redirect.addFlashAttribute("erro", "A alternativa não está disponível nesta questão.");
                } else {
                    throw exception;
                }
            }
        }
        return "redirect:" + questionPath(attemptId, questionId);
    }

    private String questionPath(Long attemptId, Long questionId) {
        return "/estudo/tentativas/" + attemptId + "/questoes/" + questionId;
    }
}
