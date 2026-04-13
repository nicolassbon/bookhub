package com.bookhub.identity.domain.user;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID userId);

    User save(User user);
}
