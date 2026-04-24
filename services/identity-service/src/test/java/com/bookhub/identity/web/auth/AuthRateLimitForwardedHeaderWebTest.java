package com.bookhub.identity.web.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.application.auth.ForgotPasswordService;
import com.bookhub.identity.application.auth.LoginUserCommand;
import com.bookhub.identity.application.auth.LoginUserResult;
import com.bookhub.identity.application.auth.LoginUserService;
import com.bookhub.identity.application.auth.LogoutUserService;
import com.bookhub.identity.application.auth.RefreshSessionService;
import com.bookhub.identity.application.auth.RegisterUserService;
import com.bookhub.identity.application.auth.ResetPasswordService;
import com.bookhub.identity.config.JwtKeyConfig;
import com.bookhub.identity.config.RefreshTokenProperties;
import com.bookhub.identity.config.SecurityConfig;
import com.bookhub.identity.web.error.GlobalExceptionHandler;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtKeyConfig.class})
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "auth.rate-limit.login.max-attempts=1",
      "auth.rate-limit.login.window-seconds=60",
      "auth.rate-limit.register.max-attempts=1",
      "auth.rate-limit.register.window-seconds=60",
      "auth.rate-limit.forgot-password.max-attempts=1",
      "auth.rate-limit.forgot-password.window-seconds=60",
      "auth.rate-limit.refresh.max-attempts=1",
      "auth.rate-limit.refresh.window-seconds=60"
    })
class AuthRateLimitForwardedHeaderWebTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RegisterUserService registerUserService;

  @MockitoBean private LoginUserService loginUserService;

  @MockitoBean private RefreshSessionService refreshSessionService;

  @MockitoBean private LogoutUserService logoutUserService;

  @MockitoBean private ForgotPasswordService forgotPasswordService;

  @MockitoBean private ResetPasswordService resetPasswordService;

  @MockitoBean private AuthWebMapper authWebMapper;

  @MockitoBean private RefreshTokenProperties refreshTokenProperties;

  @MockitoBean private Clock clock;

  @BeforeEach
  void setUp() {
    when(refreshTokenProperties.cookieName()).thenReturn("refresh_token");
    when(refreshTokenProperties.cookiePath()).thenReturn("/api/v1/auth");
    when(refreshTokenProperties.cookieSameSite()).thenReturn("Strict");
    when(refreshTokenProperties.cookieSecure()).thenReturn(false);
    when(clock.instant()).thenReturn(Instant.parse("2026-04-17T00:00:00Z"));
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);

    when(authWebMapper.toLoginUserCommand(any(LoginRequest.class)))
        .thenReturn(
            LoginUserCommand.builder()
                .email("nico@example.com")
                .password("StrongPassword123!")
                .build());

    when(loginUserService.login(any(LoginUserCommand.class)))
        .thenReturn(
            LoginUserResult.builder()
                .accessToken("jwt-access-token")
                .expiresIn(3600)
                .refreshToken("opaque-refresh-token")
                .refreshTokenExpiresIn(604800)
                .user(
                    LoginUserResult.LoginUserView.builder()
                        .userId("usr_123")
                        .username("nico")
                        .displayName("Nicolas Bon")
                        .role("USER")
                        .build())
                .build());

    when(authWebMapper.toLoginResponse(any(LoginUserResult.class)))
        .thenReturn(
            LoginResponse.builder()
                .accessToken("jwt-access-token")
                .expiresIn(3600)
                .user(
                    LoginResponse.LoginUserResponse.builder()
                        .userId("usr_123")
                        .username("nico")
                        .displayName("Nicolas Bon")
                        .role("USER")
                        .build())
                .build());
  }

  @Test
  void shouldUseForwardedClientIpWhenRequestComesFromTrustedProxy() throws Exception {
    final String requestBody =
        """
                {
                  "email": "nico@example.com",
                  "password": "StrongPassword123!"
                }
                """;

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .with(
                    request -> {
                      request.setRemoteAddr("172.20.0.10");
                      return request;
                    })
                .header("X-Forwarded-For", "198.51.100.10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .with(
                    request -> {
                      request.setRemoteAddr("172.20.0.10");
                      return request;
                    })
                .header("X-Forwarded-For", "198.51.100.20")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk());
  }
}
