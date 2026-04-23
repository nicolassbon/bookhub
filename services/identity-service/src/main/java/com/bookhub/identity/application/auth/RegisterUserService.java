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
  private final AuthResultMapper authResultMapper;

  public RegisterUserService(
      final UserRepository userRepository,
      final PasswordEncoder passwordEncoder,
      final AuthResultMapper authResultMapper) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authResultMapper = authResultMapper;
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
    final User user =
        User.create(
            normalizedUsername,
            normalizedEmail,
            hashedPassword,
            command.displayName().trim(),
            UserRole.USER);

    final User persistedUser = userRepository.save(user);
    return authResultMapper.toRegisterUserResult(persistedUser);
  }

  private String normalize(final String value) {
    return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
  }
}
