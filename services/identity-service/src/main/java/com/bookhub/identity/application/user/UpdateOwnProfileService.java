package com.bookhub.identity.application.user;

import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserNotFoundException;
import com.bookhub.identity.domain.user.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateOwnProfileService {

  private final UserRepository userRepository;

  public UpdateOwnProfileService(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User execute(final String subject, final UpdateOwnProfileCommand command) {
    if (command.hasOutOfScopeFields()) {
      throw new IllegalArgumentException("Only displayName, bio and avatarUrl can be updated");
    }

    final UUID userId = UUID.fromString(subject);
    final User user =
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(subject));

    final String nextDisplayName =
        command.displayName() != null ? command.displayName() : user.getDisplayName();
    final String nextBio = command.bioProvided() ? command.bio() : user.getBio();
    final String nextAvatarUrl =
        command.avatarUrlProvided() ? command.avatarUrl() : user.getAvatarUrl();

    user.updateProfile(nextDisplayName, nextBio, nextAvatarUrl);
    return userRepository.save(user);
  }
}
