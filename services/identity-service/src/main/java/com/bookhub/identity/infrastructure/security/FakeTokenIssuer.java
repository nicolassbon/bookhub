package com.bookhub.identity.infrastructure.security;

import com.bookhub.identity.application.auth.TokenIssuer;
import com.bookhub.identity.domain.user.User;
import org.springframework.stereotype.Component;

@Component
public class FakeTokenIssuer implements TokenIssuer {

    private static final long TEMPORARY_EXPIRATION_SECONDS = 3600;

    @Override
    public IssuedTokenPair issueFor(final User user) {
        // Temporary placeholder strategy for vertical slicing.
        // Next slice should replace this with a real JWT implementation.
        final String accessToken = "temp-access-" + user.getId();
        final String refreshToken = "temp-refresh-" + user.getId();

        return IssuedTokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(TEMPORARY_EXPIRATION_SECONDS)
                .build();
    }
}
