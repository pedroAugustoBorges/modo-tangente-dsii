package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.ConversationMessagePersistenceService;
import br.edu.fatec.modotangente.service.ConversationService;
import br.edu.fatec.modotangente.service.CurrentAppUserService;
import br.edu.fatec.modotangente.service.StudentCatalogService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/estudo/tentativas/{attemptId}/questoes/{questionId}/chat")
public class StudyChatController {

    private final CurrentAppUserService users;
    private final ConversationService conversations;
    private final ConversationMessagePersistenceService messages;
    private final StudentCatalogService catalog;

    public StudyChatController(CurrentAppUserService users, ConversationService conversations,
                               ConversationMessagePersistenceService messages, StudentCatalogService catalog) {
        this.users = users;
        this.conversations = conversations;
        this.messages = messages;
        this.catalog = catalog;
    }

    @PostMapping
    public String open(@PathVariable Long attemptId, @PathVariable Long questionId,
                       Authentication authentication) {
        conversations.getOrCreate(users.requireUserId(authentication), attemptId, questionId);
        return "redirect:/estudo/tentativas/" + attemptId + "/questoes/" + questionId + "/chat";
    }

    @GetMapping
    public String history(@PathVariable Long attemptId, @PathVariable Long questionId,
                          Authentication authentication, Model model) {
        Long userId = users.requireUserId(authentication);
        var conversation = conversations.findExisting(userId, attemptId, questionId);
        model.addAttribute("conversaId", conversation.id());
        model.addAttribute("tentativaId", attemptId);
        model.addAttribute("questao", catalog.historical(userId, attemptId, questionId));
        model.addAttribute("mensagens", messages.listForOwner(userId, conversation.id()));
        return "estudo/chat";
    }
}
