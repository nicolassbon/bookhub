package com.bookhub.catalog.web.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
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

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {
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
        return Optional.ofNullable(request.getHeader(REQUEST_ID_HEADER))
                .filter(value -> !value.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());
    }
}
