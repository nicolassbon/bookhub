package com.bookhub.identity.web.auth.ratelimit;

import com.bookhub.identity.application.auth.ratelimit.AuthRateLimitStore;
import com.bookhub.identity.application.auth.ratelimit.RateLimitDecision;
import com.bookhub.identity.config.AuthRateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthRateLimitInterceptor implements HandlerInterceptor {

  private static final String LOGIN_PATH = "/api/v1/auth/login";
  private static final String REGISTER_PATH = "/api/v1/auth/register";
  private static final String FORGOT_PASSWORD_PATH = "/api/v1/auth/forgot-password";
  private static final String REFRESH_PATH = "/api/v1/auth/refresh";
  private static final String SERVICE_TOKEN_PATH = "/api/v1/auth/service-token";

  private static final int MAX_FORWARDED_HOPS = 20;

  private final AuthRateLimitProperties authRateLimitProperties;
  private final AuthRateLimitStore authRateLimitStore;

  public AuthRateLimitInterceptor(
      final AuthRateLimitProperties authRateLimitProperties,
      final AuthRateLimitStore authRateLimitStore) {
    this.authRateLimitProperties = authRateLimitProperties;
    this.authRateLimitStore = authRateLimitStore;
  }

  @Override
  public boolean preHandle(
      final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
    final EndpointRateLimit endpointRateLimit = resolveEndpointRateLimit(request.getRequestURI());
    if (endpointRateLimit == null) {
      return true;
    }

    final AuthRateLimitProperties.EndpointRule rule = endpointRateLimit.rule();
    final RateLimitDecision decision =
        authRateLimitStore.consume(
            bucketKey(endpointRateLimit.endpointId(), request),
            rule.maxAttempts(),
            Duration.ofSeconds(rule.windowSeconds()));
    if (!decision.allowed()) {
      throw new RateLimitExceededException("Too many requests for this endpoint. Try again later.");
    }

    return true;
  }

  private EndpointRateLimit resolveEndpointRateLimit(final String requestPath) {
    return switch (requestPath) {
      case LOGIN_PATH -> new EndpointRateLimit("login", authRateLimitProperties.login());
      case REGISTER_PATH -> new EndpointRateLimit("register", authRateLimitProperties.register());
      case FORGOT_PASSWORD_PATH ->
          new EndpointRateLimit("forgot-password", authRateLimitProperties.forgotPassword());
      case REFRESH_PATH -> new EndpointRateLimit("refresh", authRateLimitProperties.refresh());
      case SERVICE_TOKEN_PATH ->
          new EndpointRateLimit("service-token", authRateLimitProperties.serviceToken());
      default -> null;
    };
  }

  private String bucketKey(final String endpointId, final HttpServletRequest request) {
    return endpointId + ":" + clientFingerprint(request);
  }

  private String clientFingerprint(final HttpServletRequest request) {
    final String remoteAddress = resolveRemoteAddress(request);
    if (isTrustedForwardedHeaderSource(remoteAddress)) {
      return resolveForwardedClientAddress(remoteAddress, request.getHeader("X-Forwarded-For"));
    }

    return remoteAddress;
  }

  private String resolveForwardedClientAddress(
      final String remoteAddress, final String forwardedForHeader) {
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
    } else if (trimmedHop.contains(".")
        && trimmedHop.chars().filter(character -> character == ':').count() == 1) {
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
    return authRateLimitProperties.trustedProxyCidrs().stream()
        .anyMatch(cidr -> isAddressInCidr(candidateAddress, cidr));
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

  private record EndpointRateLimit(String endpointId, AuthRateLimitProperties.EndpointRule rule) {}
}
