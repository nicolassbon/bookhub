package com.bookhub.catalog.web.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpRequestResponseLoggingFilter extends OncePerRequestFilter {

  private static final String REQUEST_ID_ATTRIBUTE = "requestId";
  private static final String REQUEST_ID_HEADER = "X-Request-Id";
  private static final int REQUEST_ID_MAX_LENGTH = 100;
  private static final Pattern REQUEST_ID_ALLOWED_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain)
      throws ServletException, IOException {
    final Instant startedAt = Instant.now();
    final String requestId = resolveRequestId(request);

    request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
    response.setHeader(REQUEST_ID_HEADER, requestId);

    log.info(
        "Incoming request method={} path={} requestId={}",
        request.getMethod(),
        request.getRequestURI(),
        requestId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      final long durationMs = Duration.between(startedAt, Instant.now()).toMillis();
      log.info(
          "Completed request method={} path={} status={} durationMs={} requestId={}",
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          durationMs,
          requestId);
    }
  }

  private String resolveRequestId(final HttpServletRequest request) {
    final String incomingRequestId = request.getHeader(REQUEST_ID_HEADER);
    if (isValidRequestId(incomingRequestId)) {
      return incomingRequestId;
    }
    return UUID.randomUUID().toString();
  }

  private boolean isValidRequestId(final String requestId) {
    return requestId != null
        && !requestId.isBlank()
        && requestId.length() <= REQUEST_ID_MAX_LENGTH
        && REQUEST_ID_ALLOWED_PATTERN.matcher(requestId).matches();
  }
}
