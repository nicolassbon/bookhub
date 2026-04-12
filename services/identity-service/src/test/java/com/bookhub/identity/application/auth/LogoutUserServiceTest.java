package com.bookhub.identity.application.auth;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bookhub.identity.domain.auth.RefreshTokenRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutUserServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private LogoutUserService logoutUserService;

    @Test
    @DisplayName("Should revoke refresh token when cookie value is present")
    void shouldRevokeRefreshTokenWhenCookieValueIsPresent() {
        final String tokenValue = UUID.fromString("e2b2f5d2-a101-4a3e-b1f2-250d58df1309").toString();

        logoutUserService.logout(tokenValue);

        verify(refreshTokenRepository).revokeByToken(UUID.fromString(tokenValue));
    }

    @Test
    @DisplayName("Should be a no-op when cookie value is missing")
    void shouldBeANoOpWhenCookieValueIsMissing() {
        assertThatCode(() -> logoutUserService.logout(null)).doesNotThrowAnyException();

        verify(refreshTokenRepository, never()).revokeByToken(any(UUID.class));
    }
}
