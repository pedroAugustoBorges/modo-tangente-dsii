package br.edu.fatec.modotangente.service;

import br.edu.fatec.modotangente.domain.AppUser;
import br.edu.fatec.modotangente.repository.AppUserRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {
    private static final int BCRYPT_MAX_BYTES = 72;

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(String fullName, String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new InvalidRegistrationException("A senha deve ter no máximo 72 bytes em UTF-8.");
        }
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyRegisteredException();
        }

        AppUser user = new AppUser();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("STUDENT");
        user.setActive(true);
        try {
            users.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyRegisteredException();
        }
    }

    public static class EmailAlreadyRegisteredException extends RuntimeException { }

    public static class InvalidRegistrationException extends RuntimeException {
        public InvalidRegistrationException(String message) { super(message); }
    }
}
