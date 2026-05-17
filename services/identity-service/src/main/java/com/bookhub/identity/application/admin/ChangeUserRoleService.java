package com.bookhub.identity.application.admin;

import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserNotFoundException;
import com.bookhub.identity.domain.user.UserRepository;
import com.bookhub.identity.domain.user.UserRole;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeUserRoleService {

  private final UserRepository userRepository;

  public ChangeUserRoleService(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public User changeRole(final UUID userId, final UserRole newRole) {
    final User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId.toString()));
    user.changeRole(newRole);
    return userRepository.save(user);
  }
}
