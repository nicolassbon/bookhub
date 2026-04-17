package com.bookhub.identity.web.auth.ratelimit;

import com.bookhub.identity.config.AuthRateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthRateLimitInterceptor implements HandlerInterceptor {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REGISTER_PATH = "/api/v1/auth/register";
    private static final String FORGOT_PASSWORD_PATH = "/api/v1/auth/forgot-password";

    private final AuthRateLimitProperties authRateLimitProperties;
    private final Clock clock;
    private final Map<String, Deque<Instant>> attemptsByKey;

    public AuthRateLimitInterceptor(
            final AuthRateLimitProperties authRateLimitProperties,
            final Clock clock) {
        this.authRateLimitProperties = authRateLimitProperties;
        this.clock = clock;
        this.attemptsByKey = new ConcurrentHashMap<>();
    }

    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler) {
        final AuthRateLimitProperties.EndpointRule rule = resolveRule(request.getRequestURI());
        if (rule == null) {
            return true;
        }

        final Instant now = Instant.now(clock);
        final Instant windowStart = now.minusSeconds(rule.windowSeconds());
        final String key = request.getRequestURI() + ":" + clientFingerprint(request);
        final Deque<Instant> attempts = attemptsByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (attempts) {
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(windowStart)) {
                attempts.removeFirst();
            }

            if (attempts.size() >= rule.maxAttempts()) {
                throw new RateLimitExceededException("Too many requests for this endpoint. Try again later.");
            }

            attempts.addLast(now);
        }

        return true;
    }

    private AuthRateLimitProperties.EndpointRule resolveRule(final String requestPath) {
        if (LOGIN_PATH.equals(requestPath)) {
            return authRateLimitProperties.login();
        }
        if (REGISTER_PATH.equals(requestPath)) {
            return authRateLimitProperties.register();
        }
        if (FORGOT_PASSWORD_PATH.equals(requestPath)) {
            return authRateLimitProperties.forgotPassword();
        }
        return null;
    }

    private String clientFingerprint(final HttpServletRequest request) {
        final String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
    }
}
