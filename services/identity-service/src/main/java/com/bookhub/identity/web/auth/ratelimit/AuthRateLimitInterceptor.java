package com.bookhub.identity.web.auth.ratelimit;

import com.bookhub.identity.config.AuthRateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthRateLimitInterceptor implements HandlerInterceptor {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REGISTER_PATH = "/api/v1/auth/register";
    private static final String FORGOT_PASSWORD_PATH = "/api/v1/auth/forgot-password";
    private static final String REFRESH_PATH = "/api/v1/auth/refresh";

    private static final long CLEANUP_INTERVAL = 100;
    private static final int MAX_FORWARDED_HOPS = 20;

    private final AuthRateLimitProperties authRateLimitProperties;
    private final Clock clock;
    private final Map<String, Deque<Instant>> attemptsByKey;
    private final AtomicLong requestsSinceCleanup;

    public AuthRateLimitInterceptor(
            final AuthRateLimitProperties authRateLimitProperties,
            final Clock clock) {
        this.authRateLimitProperties = authRateLimitProperties;
        this.clock = clock;
        this.attemptsByKey = new ConcurrentHashMap<>();
        this.requestsSinceCleanup = new AtomicLong(0);
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
        cleanupIfNeeded(now);

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

        enforceMaxTrackedKeys();

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
        if (REFRESH_PATH.equals(requestPath)) {
            return authRateLimitProperties.refresh();
        }
        return null;
    }

    private String clientFingerprint(final HttpServletRequest request) {
        final String remoteAddress = resolveRemoteAddress(request);
        if (isTrustedForwardedHeaderSource(remoteAddress)) {
            return resolveForwardedClientAddress(remoteAddress, request.getHeader("X-Forwarded-For"));
        }

        return remoteAddress;
    }

    private String resolveForwardedClientAddress(final String remoteAddress, final String forwardedForHeader) {
        final List<String> proxyChain = parseForwardedForHeader(forwardedForHeader);
        if (proxyChain.isEmpty()) {
            return remoteAddress;
        }

        proxyChain.add(remoteAddress);
        for (int index = proxyChain.size() - 1; index >= 0; index--) {
            final String hopAddress = proxyChain.get(index);
            if (!isTrustedProxyAddress(hopAddress)) {
                return hopAddress;
            }
        }

        return remoteAddress;
    }

    private List<String> parseForwardedForHeader(final String forwardedForHeader) {
        if (forwardedForHeader == null || forwardedForHeader.isBlank()) {
            return new ArrayList<>();
        }

        final String[] forwardedHops = forwardedForHeader.split(",");
        final List<String> sanitizedHops = new ArrayList<>();
        for (String forwardedHop : forwardedHops) {
            if (sanitizedHops.size() >= MAX_FORWARDED_HOPS) {
                break;
            }

            final String sanitizedHop = sanitizeForwardedHop(forwardedHop);
            if (sanitizedHop != null) {
                sanitizedHops.add(sanitizedHop);
            }
        }

        return sanitizedHops;
    }

    private String sanitizeForwardedHop(final String rawForwardedHop) {
        if (rawForwardedHop == null) {
            return null;
        }

        final String trimmedHop = rawForwardedHop.trim();
        if (trimmedHop.isBlank() || "unknown".equalsIgnoreCase(trimmedHop)) {
            return null;
        }

        final String hostCandidate;
        if (trimmedHop.startsWith("[") && trimmedHop.contains("]")) {
            hostCandidate = trimmedHop.substring(1, trimmedHop.indexOf(']'));
        } else if (trimmedHop.contains(".") && trimmedHop.chars().filter(character -> character == ':').count() == 1) {
            hostCandidate = trimmedHop.substring(0, trimmedHop.lastIndexOf(':'));
        } else {
            hostCandidate = trimmedHop;
        }

        if (hostCandidate.isBlank() || !isValidIpLiteral(hostCandidate)) {
            return null;
        }

        return hostCandidate;
    }

    private boolean isValidIpLiteral(final String candidateAddress) {
        if (candidateAddress.contains(":")) {
            if (!candidateAddress.matches("^[0-9a-fA-F:%]+$")) {
                return false;
            }
        } else if (candidateAddress.contains(".")) {
            if (!candidateAddress.matches("^[0-9.]+$")) {
                return false;
            }
        } else {
            return false;
        }

        try {
            final InetAddress parsedAddress = InetAddress.getByName(candidateAddress);
            if (candidateAddress.contains(":")) {
                return parsedAddress instanceof Inet6Address;
            }

            return parsedAddress instanceof Inet4Address;
        } catch (UnknownHostException exception) {
            return false;
        }
    }

    private String resolveRemoteAddress(final HttpServletRequest request) {
        final String remoteAddress = request.getRemoteAddr();
        return remoteAddress == null ? "unknown" : remoteAddress;
    }

    private boolean isTrustedForwardedHeaderSource(final String remoteAddress) {
        if (!authRateLimitProperties.trustForwardedHeaders()) {
            return false;
        }

        final List<String> trustedProxyCidrs = authRateLimitProperties.trustedProxyCidrs();
        if (trustedProxyCidrs == null || trustedProxyCidrs.isEmpty()) {
            return false;
        }

        return isTrustedProxyAddress(remoteAddress);
    }

    private boolean isTrustedProxyAddress(final String candidateAddress) {
        return authRateLimitProperties.trustedProxyCidrs().stream().anyMatch(cidr -> isAddressInCidr(candidateAddress, cidr));
    }

    private boolean isAddressInCidr(final String address, final String cidr) {
        if (cidr == null || cidr.isBlank()) {
            return false;
        }

        final String[] cidrParts = cidr.split("/");
        if (cidrParts.length != 2) {
            return false;
        }

        try {
            final InetAddress baseAddress = InetAddress.getByName(cidrParts[0]);
            final InetAddress candidateAddress = InetAddress.getByName(address);
            final int prefixLength = Integer.parseInt(cidrParts[1]);

            final byte[] baseBytes = baseAddress.getAddress();
            final byte[] candidateBytes = candidateAddress.getAddress();
            if (baseBytes.length != candidateBytes.length) {
                return false;
            }
            if (prefixLength < 0 || prefixLength > (baseBytes.length * 8)) {
                return false;
            }

            final int fullBytes = prefixLength / 8;
            final int remainingBits = prefixLength % 8;

            for (int index = 0; index < fullBytes; index++) {
                if (baseBytes[index] != candidateBytes[index]) {
                    return false;
                }
            }

            if (remainingBits == 0) {
                return true;
            }

            final int mask = ~((1 << (8 - remainingBits)) - 1);
            return ((baseBytes[fullBytes] & 0xFF) & mask) == ((candidateBytes[fullBytes] & 0xFF) & mask);
        } catch (UnknownHostException | NumberFormatException exception) {
            return false;
        }
    }

    private void cleanupIfNeeded(final Instant now) {
        final long seenRequests = requestsSinceCleanup.incrementAndGet();
        if (seenRequests % CLEANUP_INTERVAL != 0) {
            return;
        }

        final long staleEntryTtlSeconds = authRateLimitProperties.staleEntryTtlSeconds();
        final Instant staleThreshold = now.minusSeconds(staleEntryTtlSeconds);
        attemptsByKey.forEach((key, attempts) -> {
            synchronized (attempts) {
                removeStaleAttempts(attempts, staleThreshold);
                if (attempts.isEmpty()) {
                    attemptsByKey.remove(key, attempts);
                }
            }
        });
    }

    private void removeStaleAttempts(final Deque<Instant> attempts, final Instant staleThreshold) {
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(staleThreshold)) {
            attempts.removeFirst();
        }
    }

    private void enforceMaxTrackedKeys() {
        final int maxTrackedKeys = authRateLimitProperties.maxTrackedKeys();
        if (attemptsByKey.size() <= maxTrackedKeys) {
            return;
        }

        attemptsByKey.entrySet().stream()
                .sorted((left, right) -> {
                    final Instant leftLastAttempt = lastAttempt(left.getValue());
                    final Instant rightLastAttempt = lastAttempt(right.getValue());
                    if (leftLastAttempt == null && rightLastAttempt == null) {
                        return 0;
                    }
                    if (leftLastAttempt == null) {
                        return -1;
                    }
                    if (rightLastAttempt == null) {
                        return 1;
                    }
                    return leftLastAttempt.compareTo(rightLastAttempt);
                })
                .limit(attemptsByKey.size() - maxTrackedKeys)
                .map(Map.Entry::getKey)
                .forEach(attemptsByKey::remove);
    }

    private Instant lastAttempt(final Deque<Instant> attempts) {
        synchronized (attempts) {
            return attempts.peekLast();
        }
    }
}
