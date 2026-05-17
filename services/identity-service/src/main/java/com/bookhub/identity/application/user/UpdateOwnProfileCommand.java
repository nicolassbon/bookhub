package com.bookhub.identity.application.user;

import lombok.Builder;

@Builder
public record UpdateOwnProfileCommand(
    String displayName,
    String bio,
    String avatarUrl,
    boolean bioProvided,
    boolean avatarUrlProvided,
    String email,
    String username,
    String role,
    String status,
    String credentials) {

  public boolean hasOutOfScopeFields() {
    return email != null
        || username != null
        || role != null
        || status != null
        || credentials != null;
  }
}
