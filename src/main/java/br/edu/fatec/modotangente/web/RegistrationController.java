package br.edu.fatec.modotangente.web;

import br.edu.fatec.modotangente.service.RegistrationService;
import br.edu.fatec.modotangente.service.RegistrationService.EmailAlreadyRegisteredException;
import br.edu.fatec.modotangente.service.RegistrationService.InvalidRegistrationException;
import br.edu.fatec.modotangente.web.dto.RegistrationForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cadastro")
public class RegistrationController {
    private final RegistrationService registrations;

    public RegistrationController(RegistrationService registrations) {
        this.registrations = registrations;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "autenticacao/cadastro";
    }

    @PostMapping
    public String register(@Valid @ModelAttribute("form") RegistrationForm form,
                           BindingResult errors) {
        if (errors.hasErrors()) return "autenticacao/cadastro";

        try {
            registrations.register(form.getFullName(), form.getEmail(), form.getPassword());
        } catch (EmailAlreadyRegisteredException exception) {
            errors.rejectValue("email", "duplicate", "Já existe uma conta com este e-mail.");
            return "autenticacao/cadastro";
        } catch (InvalidRegistrationException exception) {
            errors.rejectValue("password", "invalid", exception.getMessage());
            return "autenticacao/cadastro";
        }

        return "redirect:/login?registered";
    }
}
