package com.bookhub.identity.domain.user;

import java.util.Optional;

public interface UserRepository {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

    User save(User user);
}
