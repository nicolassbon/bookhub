package com.bookhub.identity.application.user;

import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserNotFoundException;
import com.bookhub.identity.domain.user.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetPublicProfileService {

  private final UserRepository userRepository;

  public GetPublicProfileService(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User execute(final UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId.toString()));
  }
}
