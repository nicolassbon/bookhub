package com.bookhub.identity.domain.auth;

public interface MailSenderPort {

    void sendPasswordResetEmail(String to, String token);
}
