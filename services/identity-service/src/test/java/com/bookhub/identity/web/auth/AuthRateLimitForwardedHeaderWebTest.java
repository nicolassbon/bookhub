package com.bookhub.identity.web.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.application.auth.ForgotPasswordService;
import com.bookhub.identity.application.auth.LoginUserCommand;
import com.bookhub.identity.application.auth.LoginUserResult;
import com.bookhub.identity.application.auth.LoginUserService;
import com.bookhub.identity.application.auth.LogoutUserService;
import com.bookhub.identity.application.auth.RefreshSessionService;
import com.bookhub.identity.application.auth.RegisterUserService;
import com.bookhub.identity.application.auth.ResetPasswordService;
import com.bookhub.identity.application.auth.ratelimit.AuthRateLimitStore;
import com.bookhub.identity.application.auth.ratelimit.RateLimitDecision;
import com.bookhub.identity.config.JwtKeyConfig;
import com.bookhub.identity.config.RefreshTokenProperties;
import com.bookhub.identity.config.SecurityConfig;
import com.bookhub.identity.web.error.GlobalExceptionHandler;
import java.time.Clock;
import java.time.Duration;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

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

  private static final String PROXY_REMOTE_ADDRESS = "172.20.0.10";

  private static final String LOGIN_REQUEST_BODY =
      """
              {
                "email": "nico@example.com",
                "password": "StrongPassword123!"
              }
              """;

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RegisterUserService registerUserService;

  @MockitoBean private LoginUserService loginUserService;

  @MockitoBean private RefreshSessionService refreshSessionService;

  @MockitoBean private LogoutUserService logoutUserService;

  @MockitoBean private ForgotPasswordService forgotPasswordService;

  @MockitoBean private ResetPasswordService resetPasswordService;

  @MockitoBean private AuthWebMapper authWebMapper;

  @MockitoBean private RefreshTokenProperties refreshTokenProperties;

  @MockitoBean private AuthRateLimitStore authRateLimitStore;

  @MockitoBean private Clock clock;

  @BeforeEach
  void setUp() {
    when(refreshTokenProperties.cookieName()).thenReturn("refresh_token");
    when(refreshTokenProperties.cookiePath()).thenReturn("/api/v1/auth");
    when(refreshTokenProperties.cookieSameSite()).thenReturn("Strict");
    when(refreshTokenProperties.cookieSecure()).thenReturn(false);
    when(clock.instant()).thenReturn(Instant.parse("2026-04-17T00:00:00Z"));
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    when(authRateLimitStore.consume(any(), anyInt(), any(Duration.class)))
        .thenReturn(RateLimitDecision.allowed(99, Duration.ofSeconds(60)));

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
    performLoginFromTrustedProxy("198.51.100.10").andExpect(status().isOk());

    performLoginFromTrustedProxy("198.51.100.20").andExpect(status().isOk());
  }

  @Test
  void shouldRateLimitRequestsWhenTrustedProxyForwardsTheSameClientIp() throws Exception {
    allowNextAttemptThenBlock();

    performLoginFromTrustedProxy("198.51.100.30").andExpect(status().isOk());

    performLoginFromTrustedProxy("198.51.100.30")
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.status").value(429))
        .andExpect(jsonPath("$.code").value("RATE_LIMIT_EXCEEDED"))
        .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
  }

  @Test
  void shouldIgnoreForwardedClientIpWhenRequestComesFromUntrustedSource() throws Exception {
    final String untrustedRemoteAddress = "10.10.10.10";

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .with(remoteAddress(untrustedRemoteAddress))
                .header("X-Forwarded-For", "198.51.100.99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LOGIN_REQUEST_BODY))
        .andExpect(status().isOk());

    verify(authRateLimitStore)
        .consume(eq("login:" + untrustedRemoteAddress), anyInt(), any(Duration.class));
  }

  private void allowNextAttemptThenBlock() {
    when(authRateLimitStore.consume(any(), anyInt(), any(Duration.class)))
        .thenReturn(
            RateLimitDecision.allowed(0, Duration.ofSeconds(60)),
            RateLimitDecision.blocked(Duration.ofSeconds(60)));
  }

  private org.springframework.test.web.servlet.ResultActions performLoginFromTrustedProxy(
      final String forwardedClientIp) throws Exception {
    return mockMvc.perform(
        post("/api/v1/auth/login")
            .with(trustedProxyRemoteAddress())
            .header("X-Forwarded-For", forwardedClientIp)
            .contentType(MediaType.APPLICATION_JSON)
            .content(LOGIN_REQUEST_BODY));
  }

  private RequestPostProcessor trustedProxyRemoteAddress() {
    return remoteAddress(PROXY_REMOTE_ADDRESS);
  }

  private RequestPostProcessor remoteAddress(final String address) {
    return request -> {
      request.setRemoteAddr(address);
      return request;
    };
  }
}
