package com.bookhub.identity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetProperties {

    private final long expirationSeconds;
    private final String resetUrlBase;
    private final String fromAddress;

    public PasswordResetProperties(
            @Value("${auth.password-reset.expiration-seconds:900}") final long expirationSeconds,
            @Value("${auth.password-reset.url-base}") final String resetUrlBase,
            @Value("${auth.password-reset.from-address}") final String fromAddress) {
        this.expirationSeconds = expirationSeconds;
        this.resetUrlBase = resetUrlBase;
        this.fromAddress = fromAddress;
    }

    public long expirationSeconds() {
        return expirationSeconds;
    }

    public String resetUrlBase() {
        return resetUrlBase;
    }

    public String fromAddress() {
        return fromAddress;
    }
}
