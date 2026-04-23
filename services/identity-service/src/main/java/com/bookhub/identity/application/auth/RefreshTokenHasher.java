package com.bookhub.identity.application.auth;

public interface RefreshTokenHasher {

  String hash(String rawToken);
}
