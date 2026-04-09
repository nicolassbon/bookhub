package com.bookhub.identity.domain.user;

public interface UserRepository {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    User save(User user);
}
