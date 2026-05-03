package com.bookhub.catalog.support;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class TestSecretsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  private static final String PROPERTY_SOURCE_NAME = "test-secrets-property-source";

  @Override
  public void postProcessEnvironment(
      final ConfigurableEnvironment environment, final SpringApplication application) {
    final Map<String, Object> properties = new LinkedHashMap<>();

    putIfMissing(environment, properties, "jwt.rsa.public-key", TestRsaKeys.PUBLIC_2048);

    if (!properties.isEmpty()) {
      environment
          .getPropertySources()
          .addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  private void putIfMissing(
      final ConfigurableEnvironment environment,
      final Map<String, Object> properties,
      final String key,
      final String value) {
    if (!environment.containsProperty(key)) {
      properties.put(key, value);
    }
  }
}
