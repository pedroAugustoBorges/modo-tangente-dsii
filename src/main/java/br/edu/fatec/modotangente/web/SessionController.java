package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.domain.AppUser;
import br.edu.fatec.modotangente.service.CurrentAppUserService;
import br.edu.fatec.modotangente.web.dto.CurrentUserView;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SessionController {

    private final CurrentAppUserService users;

    public SessionController(CurrentAppUserService users) {
        this.users = users;
    }

    @GetMapping("/login")
    public String login() {
        return "autenticacao/login";
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        model.addAttribute("usuario", currentUser(authentication));
        return "inicio";
    }

    @GetMapping("/api/me")
    @ResponseBody
    public CurrentUserView currentUser(Authentication authentication) {
        AppUser user = users.requireUser(authentication);
        return new CurrentUserView(user.getId(), user.getFullName(), authentication.getName(), user.getRole());
    }
}
