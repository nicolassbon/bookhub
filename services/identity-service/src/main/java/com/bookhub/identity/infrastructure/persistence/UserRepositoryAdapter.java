package com.bookhub.identity.infrastructure.persistence;

import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRepository;
import com.bookhub.identity.domain.user.UserRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

  @Override
  public List<User> findAll(final int page, final int size) {
    final PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return userJpaRepository.findAll(pageable).getContent();
  }

  @Override
  public List<User> findAllByRole(final UserRole role, final int page, final int size) {
    final PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return userJpaRepository.findAllByRole(role, pageable).getContent();
  }

  @Override
  public long countAll() {
    return userJpaRepository.count();
  }

  @Override
  public long countByRole(final UserRole role) {
    final Page<User> page = userJpaRepository.findAllByRole(role, PageRequest.of(0, 1));
    return page.getTotalElements();
  }
}
