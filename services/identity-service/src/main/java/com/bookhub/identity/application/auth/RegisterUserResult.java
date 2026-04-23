package com.bookhub.identity.application.auth;

import lombok.Builder;

@Builder
public record RegisterUserResult(
    String userId, String username, String email, String displayName, String role) {}
