package com.bookhub.identity.application.auth;

public class PasswordResetEmailDeliveryException extends RuntimeException {

    public PasswordResetEmailDeliveryException(final String email, final Throwable cause) {
        super("Password reset email delivery failed for " + email, cause);
    }
}
