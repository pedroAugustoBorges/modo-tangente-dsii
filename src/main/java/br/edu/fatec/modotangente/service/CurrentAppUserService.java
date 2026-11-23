package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.domain.AppUser;
import br.edu.fatec.modotangente.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Optional;

@Service
public class CurrentAppUserService {

    private final AppUserRepository appUserRepository;

    public CurrentAppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public Long requireUserId(Authentication authentication) {
        return requireUser(authentication).getId();
    }

    public AppUser requireUser(Authentication authentication) {
        return findActiveUser(authentication)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "A conta autenticada não possui usuário ativo na aplicação."));
    }

    public boolean isActiveAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))
                && findActiveUser(authentication).filter(user -> "ADMIN".equals(user.getRole())).isPresent();
    }

    private Optional<AppUser> findActiveUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            return Optional.empty();
        }

        String normalizedEmail = authentication.getName().trim().toLowerCase(Locale.ROOT);
        return appUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(user -> Boolean.TRUE.equals(user.getActive()));
    }
}
