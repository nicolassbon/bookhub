package com.bookhub.identity.domain.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  Optional<User> findByEmail(String email);

  Optional<User> findById(UUID userId);

  User save(User user);

  List<User> findAll(int page, int size);

  List<User> findAllByRole(UserRole role, int page, int size);

  long countAll();

  long countByRole(UserRole role);
}
