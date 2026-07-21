package com.bookhub.library.infrastructure.client;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
class TracePrivacyTestConfiguration {

  @Bean
  TracePrivacySpanExporter tracePrivacySpanExporter() {
    return new TracePrivacySpanExporter();
  }
}
