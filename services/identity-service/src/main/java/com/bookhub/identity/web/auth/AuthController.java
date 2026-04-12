package com.bookhub.identity.web.auth;

import com.bookhub.identity.application.auth.LoginUserCommand;
import com.bookhub.identity.application.auth.LoginUserResult;
import com.bookhub.identity.application.auth.LoginUserService;
import com.bookhub.identity.application.auth.LogoutUserService;
import com.bookhub.identity.application.auth.RefreshSessionResult;
import com.bookhub.identity.application.auth.RefreshSessionService;
import com.bookhub.identity.application.auth.RegisterUserCommand;
import com.bookhub.identity.application.auth.RegisterUserResult;
import com.bookhub.identity.application.auth.RegisterUserService;
import com.bookhub.identity.config.RefreshTokenProperties;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserService registerUserService;
    private final LoginUserService loginUserService;
    private final RefreshSessionService refreshSessionService;
    private final LogoutUserService logoutUserService;
    private final AuthWebMapper authWebMapper;
    private final RefreshTokenProperties refreshTokenProperties;

    public AuthController(
            final RegisterUserService registerUserService,
            final LoginUserService loginUserService,
            final RefreshSessionService refreshSessionService,
            final LogoutUserService logoutUserService,
            final AuthWebMapper authWebMapper,
            final RefreshTokenProperties refreshTokenProperties) {
        this.registerUserService = registerUserService;
        this.loginUserService = loginUserService;
        this.refreshSessionService = refreshSessionService;
        this.logoutUserService = logoutUserService;
        this.authWebMapper = authWebMapper;
        this.refreshTokenProperties = refreshTokenProperties;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody final RegisterRequest request) {
        final RegisterUserCommand command = authWebMapper.toRegisterUserCommand(request);
        final RegisterUserResult result = registerUserService.register(command);
        final RegisterResponse response = authWebMapper.toRegisterResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody final LoginRequest request) {
        final LoginUserCommand command = authWebMapper.toLoginUserCommand(request);
        final LoginUserResult result = loginUserService.login(command);
        final LoginResponse response = authWebMapper.toLoginResponse(result);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken(), result.refreshTokenExpiresIn()).toString())
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = "${auth.refresh-token.cookie.name:refresh_token}") final String refreshToken) {
        final RefreshSessionResult result = refreshSessionService.refresh(refreshToken);
        final LoginResponse response = authWebMapper.toLoginResponse(result);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken(), result.refreshTokenExpiresIn()).toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "${auth.refresh-token.cookie.name:refresh_token}", required = false)
            final String refreshToken) {
        logoutUserService.logout(refreshToken);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .build();
    }

    private ResponseCookie refreshCookie(final String token, final long maxAgeSeconds) {
        return ResponseCookie.from(refreshTokenProperties.cookieName(), token)
                .httpOnly(true)
                .secure(refreshTokenProperties.cookieSecure())
                .path(refreshTokenProperties.cookiePath())
                .sameSite(refreshTokenProperties.cookieSameSite())
                .maxAge(maxAgeSeconds)
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(refreshTokenProperties.cookieName(), "")
                .httpOnly(true)
                .secure(refreshTokenProperties.cookieSecure())
                .path(refreshTokenProperties.cookiePath())
                .sameSite(refreshTokenProperties.cookieSameSite())
                .maxAge(0)
                .build();
    }
}
