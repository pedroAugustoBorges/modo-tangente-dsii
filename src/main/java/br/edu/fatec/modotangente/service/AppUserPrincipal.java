package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.domain.AppUser;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;

final class AppUserPrincipal extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;

    AppUserPrincipal(AppUser user, String normalizedEmail) {
        super(normalizedEmail, user.getPasswordHash(), Boolean.TRUE.equals(user.getActive()),
                true, true, true, AuthorityUtils.createAuthorityList("ROLE_" + user.getRole()));
        this.userId = user.getId();
    }

    Long userId() {
        return userId;
    }
}
