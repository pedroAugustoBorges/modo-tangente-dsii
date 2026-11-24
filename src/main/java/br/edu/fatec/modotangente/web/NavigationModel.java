package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.CurrentAppUserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = {
        SessionController.class, StudyCatalogController.class, StudyAttemptController.class, StudyChatController.class,
        AdminCatalogPagesController.class, AdminQuestionPagesController.class
})
public class NavigationModel {

    private final CurrentAppUserService users;

    public NavigationModel(CurrentAppUserService users) {
        this.users = users;
    }

    @ModelAttribute("administrador")
    public boolean showAdminLink(Authentication authentication) {
        return users.isActiveAdmin(authentication);
    }
}
