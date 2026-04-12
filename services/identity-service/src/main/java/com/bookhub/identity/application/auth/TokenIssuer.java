package com.bookhub.identity.application.auth;

import com.bookhub.identity.domain.user.User;
import lombok.Builder;

public interface TokenIssuer {

    IssuedTokenPair issueFor(User user);

    @Builder
    record IssuedTokenPair(
            String accessToken,
            long expiresIn) {
    }
}
