package com.bookhub.identity.application.auth;

import lombok.Builder;

@Builder
public record RegisterUserCommand(
    String username, String email, String password, String displayName) {}
