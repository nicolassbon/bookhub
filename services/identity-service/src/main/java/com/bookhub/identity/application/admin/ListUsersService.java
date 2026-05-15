package com.bookhub.identity.application.admin;

import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRepository;
import com.bookhub.identity.domain.user.UserRole;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListUsersService {

  private final UserRepository userRepository;

  public ListUsersService(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public PagedUsersResult list(final int page, final int size, final UserRole roleFilter) {
    final List<User> users;
    final long totalElements;

    if (roleFilter != null) {
      users = userRepository.findAllByRole(roleFilter, page, size);
      totalElements = userRepository.countByRole(roleFilter);
    } else {
      users = userRepository.findAll(page, size);
      totalElements = userRepository.countAll();
    }

    final long totalPages = totalElements == 0 ? 0 : (long) Math.ceil((double) totalElements / size);

    return new PagedUsersResult(users, page, size, totalElements, totalPages);
  }

  public record PagedUsersResult(
      List<User> users, int page, int size, long totalElements, long totalPages) {}
}
