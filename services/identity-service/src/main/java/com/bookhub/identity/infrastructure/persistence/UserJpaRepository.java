package com.bookhub.identity.infrastructure.persistence;

import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, UUID> {

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  Optional<User> findByEmail(String email);

  Page<User> findAllByRole(UserRole role, Pageable pageable);
}
