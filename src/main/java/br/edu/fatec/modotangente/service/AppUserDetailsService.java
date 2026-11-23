package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.domain.AppUser;
import br.edu.fatec.modotangente.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    public AppUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        AppUser user = users.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Conta não encontrada."));

        return User.withUsername(normalizedEmail)
                .password(user.getPasswordHash())
                .roles(user.getRole())
                .disabled(!Boolean.TRUE.equals(user.getActive()))
                .build();
    }
}
