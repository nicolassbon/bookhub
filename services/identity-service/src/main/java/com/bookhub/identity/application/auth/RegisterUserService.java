package com.bookhub.identity.application.auth;

import com.bookhub.identity.domain.user.DuplicateResourceException;
import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRepository;
import com.bookhub.identity.domain.user.UserRole;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RegisterUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterUserResult register(final RegisterUserCommand command) {
        final String normalizedEmail = normalize(command.email());
        final String normalizedUsername = normalize(command.username());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("email", "Email already in use");
        }

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new DuplicateResourceException("username", "Username already in use");
        }

        final String hashedPassword = passwordEncoder.encode(command.password());
        final User user = User.builder()
                .username(normalizedUsername)
                .email(normalizedEmail)
                .passwordHash(hashedPassword)
                .displayName(command.displayName().trim())
                .role(UserRole.USER)
                .build();

        final User persistedUser = userRepository.save(user);
        return RegisterUserResult.builder()
                .userId(persistedUser.getId().toString())
                .username(persistedUser.getUsername())
                .email(persistedUser.getEmail())
                .displayName(persistedUser.getDisplayName())
                .role(persistedUser.getRole().name())
                .build();
    }

    private String normalize(final String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
