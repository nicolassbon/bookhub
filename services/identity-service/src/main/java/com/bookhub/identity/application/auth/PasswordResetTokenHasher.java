package com.bookhub.identity.application.auth;

public interface PasswordResetTokenHasher {

  String hash(String rawToken);
}
