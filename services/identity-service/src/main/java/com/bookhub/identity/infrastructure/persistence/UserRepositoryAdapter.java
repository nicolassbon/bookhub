package com.bookhub.identity.infrastructure.persistence;

import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(final UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public boolean existsByEmail(final String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(final String username) {
        return userJpaRepository.existsByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        return userJpaRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findById(final UUID userId) {
        return userJpaRepository.findById(userId);
    }

    @Override
    public User save(final User user) {
        return userJpaRepository.save(user);
    }
}
