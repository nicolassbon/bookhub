package com.bookhub.identity.infrastructure.persistence;

import com.bookhub.identity.domain.user.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
