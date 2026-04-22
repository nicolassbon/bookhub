package com.bookhub.identity.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

class PostgreSqlIntegrationHardeningTest {

    @Test
    @DisplayName("Should not silently skip PostgreSQL integration tests when Docker is unavailable")
    void shouldNotSilentlySkipPostgreSqlIntegrationTestsWhenDockerIsUnavailable() {
        final Testcontainers annotation = PostgreSqlIntegrationTest.class.getAnnotation(Testcontainers.class);

        assertThat(annotation)
                .as("PostgreSqlIntegrationTest must remain Testcontainers-enabled")
                .isNotNull();
        assertThat(annotation.disabledWithoutDocker())
                .as("PostgreSqlIntegrationTest must fail fast instead of skipping when Docker is unavailable")
                .isFalse();
    }

    @Test
    @DisplayName("Should keep test profile datasource free from H2 fallback defaults")
    void shouldKeepTestProfileDatasourceFreeFromH2FallbackDefaults() throws IOException {
        final var configResource = getClass().getClassLoader().getResourceAsStream("application-test.yml");

        assertThat(configResource)
                .as("application-test.yml must be present on the test classpath")
                .isNotNull();

        final String yamlContent;
        try (configResource) {
            yamlContent = new String(configResource.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertThat(yamlContent)
                .as("test profile must not fallback to H2 URL")
                .doesNotContain("jdbc:h2:");
        assertThat(yamlContent)
                .as("test profile must not fallback to H2 driver")
                .doesNotContain("org.h2.Driver");
    }
}
