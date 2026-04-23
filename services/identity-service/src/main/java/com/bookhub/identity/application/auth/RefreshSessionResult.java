package com.bookhub.identity.application.auth;

import lombok.Builder;

@Builder
public record RefreshSessionResult(
    String accessToken,
    long expiresIn,
    String refreshToken,
    long refreshTokenExpiresIn,
    LoginUserResult.LoginUserView user) {}
