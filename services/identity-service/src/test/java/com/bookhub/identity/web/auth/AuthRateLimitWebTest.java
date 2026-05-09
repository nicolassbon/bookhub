package com.bookhub.identity.web.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.application.auth.ForgotPasswordService;
import com.bookhub.identity.application.auth.InvalidCredentialsException;
import com.bookhub.identity.application.auth.InvalidRefreshTokenException;
import com.bookhub.identity.application.auth.LoginUserCommand;
import com.bookhub.identity.application.auth.LoginUserService;
import com.bookhub.identity.application.auth.LogoutUserService;
import com.bookhub.identity.application.auth.RefreshSessionService;
import com.bookhub.identity.application.auth.RegisterUserCommand;
import com.bookhub.identity.application.auth.RegisterUserResult;
import com.bookhub.identity.application.auth.RegisterUserService;
import com.bookhub.identity.application.auth.ResetPasswordService;
import com.bookhub.identity.application.auth.ratelimit.AuthRateLimitStore;
import com.bookhub.identity.application.auth.ratelimit.AuthRateLimitStoreUnavailableException;
import com.bookhub.identity.application.auth.ratelimit.RateLimitDecision;
import com.bookhub.identity.config.JwtKeyConfig;
import com.bookhub.identity.config.RefreshTokenProperties;
import com.bookhub.identity.config.SecurityConfig;
import com.bookhub.identity.web.error.GlobalExceptionHandler;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
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
      "auth.rate-limit.refresh.window-seconds=60",
      "auth.rate-limit.trust-forwarded-headers=true",
      "auth.rate-limit.trusted-proxy-cidrs=127.0.0.1/32"
    })
class AuthRateLimitWebTest {

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
  }

  @Test
  void shouldReturn429WhenLoginRateLimitIsExceeded() throws Exception {
    when(authWebMapper.toLoginUserCommand(any(LoginRequest.class)))
        .thenReturn(
            LoginUserCommand.builder()
                .email("nico@example.com")
                .password("StrongPassword123!")
                .build());

    when(loginUserService.login(any(LoginUserCommand.class)))
        .thenThrow(new InvalidCredentialsException());

    final String requestBody =
        """
                {
                  "email": "nico@example.com",
                  "password": "StrongPassword123!"
                }
                """;

    final String clientIp = uniqueIp();
    allowNextAttemptThenBlock();

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .with(remoteAddress(clientIp))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
        .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .with(remoteAddress(clientIp))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.status").value(429))
        .andExpect(jsonPath("$.code").value("RATE_LIMIT_EXCEEDED"))
        .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
  }

  @Test
  void shouldReturn429WhenRegisterRateLimitIsExceeded() throws Exception {
    when(authWebMapper.toRegisterUserCommand(any(RegisterRequest.class)))
        .thenReturn(
            RegisterUserCommand.builder()
                .username("nico")
                .email("nico@example.com")
                .password("StrongPassword123!")
                .displayName("Nicolas Bon")
                .build());

    when(registerUserService.register(any(RegisterUserCommand.class)))
        .thenReturn(
            RegisterUserResult.builder()
                .userId("usr_123")
                .username("nico")
                .email("nico@example.com")
                .displayName("Nicolas Bon")
                .role("USER")
                .build());

    when(authWebMapper.toRegisterResponse(any(RegisterUserResult.class)))
        .thenReturn(
            RegisterResponse.builder()
                .userId("usr_123")
                .username("nico")
                .email("nico@example.com")
                .displayName("Nicolas Bon")
                .role("USER")
                .build());

    final String requestBody =
        """
                {
                  "username": "nico",
                  "email": "nico@example.com",
                  "password": "StrongPassword123!",
                  "displayName": "Nicolas Bon"
                }
                """;

    final String clientIp = uniqueIp();
    allowNextAttemptThenBlock();

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .with(remoteAddress(clientIp))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .with(remoteAddress(clientIp))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.status").value(429))
        .andExpect(jsonPath("$.code").value("RATE_LIMIT_EXCEEDED"))
        .andExpect(jsonPath("$.path").value("/api/v1/auth/register"));
  }

  @Test
  void shouldReturn429WhenForgotPasswordRateLimitIsExceeded() throws Exception {
    final String requestBody =
        """
                {
                  "email": "unknown@example.com"
                }
                """;

    final String clientIp = uniqueIp();
    allowNextAttemptThenBlock();

    mockMvc
        .perform(
            post("/api/v1/auth/forgot-password")
                .with(remoteAddress(clientIp))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/v1/auth/forgot-password")
                .with(remoteAddress(clientIp))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.status").value(429))
        .andExpect(jsonPath("$.code").value("RATE_LIMIT_EXCEEDED"))
        .andExpect(jsonPath("$.path").value("/api/v1/auth/forgot-password"));
  }

  @Test
  void shouldReturn429WhenRefreshRateLimitIsExceeded() throws Exception {
    when(refreshSessionService.refresh("old-refresh-token"))
        .thenThrow(new InvalidRefreshTokenException());

    final String clientIp = uniqueIp();
    allowNextAttemptThenBlock();

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .with(remoteAddress(clientIp))
                .cookie(new jakarta.servlet.http.Cookie("refresh_token", "old-refresh-token")))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"))
        .andExpect(jsonPath("$.path").value("/api/v1/auth/refresh"));

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .with(remoteAddress(clientIp))
                .cookie(new jakarta.servlet.http.Cookie("refresh_token", "old-refresh-token")))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.status").value(429))
        .andExpect(jsonPath("$.code").value("RATE_LIMIT_EXCEEDED"))
        .andExpect(jsonPath("$.path").value("/api/v1/auth/refresh"));
  }

  @Test
  void shouldRateLimitWhenForwardedIpIsSame() throws Exception {
    when(authWebMapper.toLoginUserCommand(any(LoginRequest.class)))
        .thenReturn(
            LoginUserCommand.builder()
                .email("nico@example.com")
                .password("StrongPassword123!")
                .build());
    when(loginUserService.login(any(LoginUserCommand.class)))
        .thenThrow(new InvalidCredentialsException());

    final String requestBody =
        """
                {
                  "email": "nico@example.com",
                  "password": "StrongPassword123!"
                }
                """;
    allowNextAttemptThenBlock();

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .with(forwardedFor("203.0.113.10"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .with(forwardedFor("203.0.113.10"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.status").value(429))
        .andExpect(jsonPath("$.code").value("RATE_LIMIT_EXCEEDED"))
        .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
  }

  @Test
  void shouldAllowDifferentForwardedIpsWithinWindow() throws Exception {
    final String firstClientIp = uniqueIp();
    final String secondClientIp = uniqueIp();
    when(authWebMapper.toLoginUserCommand(any(LoginRequest.class)))
        .thenReturn(
            LoginUserCommand.builder()
                .email("nico@example.com")
                .password("StrongPassword123!")
                .build());
    when(loginUserService.login(any(LoginUserCommand.class)))
        .thenThrow(new InvalidCredentialsException());

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
                .with(forwardedFor(firstClientIp))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .with(forwardedFor(secondClientIp))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
  }

  @Test
  void shouldReturn503WhenRateLimitStoreIsUnavailable() throws Exception {
    when(authRateLimitStore.consume(any(), anyInt(), any(Duration.class)))
        .thenThrow(
            new AuthRateLimitStoreUnavailableException(
                "Redis unavailable", new RuntimeException()));

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
                .with(remoteAddress(uniqueIp()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.status").value(503))
        .andExpect(jsonPath("$.code").value("AUTH_RATE_LIMIT_UNAVAILABLE"))
        .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
  }

  private void allowNextAttemptThenBlock() {
    when(authRateLimitStore.consume(any(), anyInt(), any(Duration.class)))
        .thenReturn(
            RateLimitDecision.allowed(0, Duration.ofSeconds(60)),
            RateLimitDecision.blocked(Duration.ofSeconds(60)));
  }

  private static String uniqueIp() {
    return "10.0.0." + Math.abs(UUID.randomUUID().hashCode() % 250 + 1);
  }

  private static RequestPostProcessor remoteAddress(final String address) {
    return request -> {
      request.setRemoteAddr(address);
      return request;
    };
  }

  private static RequestPostProcessor forwardedFor(final String clientIp) {
    return request -> {
      request.setRemoteAddr("127.0.0.1");
      request.addHeader("X-Forwarded-For", clientIp);
      return request;
    };
  }
}
