package com.bookhub.identity.application.auth;

import lombok.Builder;

@Builder
public record LoginUserCommand(
        String email,
        String password) {
}
