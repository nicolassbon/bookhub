package com.bookhub.library.infrastructure.client;

public record IssuedServiceToken(String accessToken, long expiresIn) {}
