package com.bookhub.identity.web.logging;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class HttpRequestResponseLoggingFilterTest {

    private final HttpRequestResponseLoggingFilter filter = new HttpRequestResponseLoggingFilter();

    @Test
    @DisplayName("Should propagate incoming X-Request-Id to response and request attributes")
    void shouldPropagateIncomingRequestId() throws ServletException, IOException {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/me");
        request.addHeader("X-Request-Id", "req-123");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(request.getAttribute("requestId")).isEqualTo("req-123");
        assertThat(response.getHeader("X-Request-Id")).isEqualTo("req-123");
    }

    @Test
    @DisplayName("Should generate request id when header is absent")
    void shouldGenerateRequestIdWhenHeaderIsAbsent() throws ServletException, IOException {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/me");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        final String requestId = (String) request.getAttribute("requestId");
        assertThat(requestId).isNotBlank();
        assertThat(response.getHeader("X-Request-Id")).isEqualTo(requestId);
    }
}
