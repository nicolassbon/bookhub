package com.bookhub.identity.web.admin;

import com.bookhub.identity.domain.user.User;
import java.time.Instant;

public record AdminUserResponse(
    String userId,
    String username,
    String email,
    String displayName,
    String role,
    Instant createdAt) {

  public static AdminUserResponse from(final User user) {
    return new AdminUserResponse(
        user.getId().toString(),
        user.getUsername(),
        user.getEmail(),
        user.getDisplayName(),
        user.getRole().name(),
        user.getCreatedAt());
  }
}
