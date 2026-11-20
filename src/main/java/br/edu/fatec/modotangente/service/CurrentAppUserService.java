package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.repository.AppUserRepository;
import br.edu.fatec.modotangente.domain.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class CurrentAppUserService {

    private final AppUserRepository appUserRepository;

    public CurrentAppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public Long requireUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String normalizedEmail = authentication.getName().trim().toLowerCase(Locale.ROOT);
        return appUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(user -> Boolean.TRUE.equals(user.getActive()))
                .map(user -> user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "A conta autenticada não possui usuário no domínio da aplicação."));
    }
}
