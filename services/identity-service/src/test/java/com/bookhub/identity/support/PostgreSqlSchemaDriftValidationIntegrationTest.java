package com.bookhub.identity.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bookhub.identity.IdentityServiceApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.testcontainers.containers.PostgreSQLContainer;

class PostgreSqlSchemaDriftValidationIntegrationTest {

    private static final PostgreSQLContainer<?> DRIFT_POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("identity_drift_test");

    static {
        DRIFT_POSTGRESQL_CONTAINER.start();
    }

    @Test
    @DisplayName("Should fail fast on schema drift when ddl-auto validate is enabled")
    void shouldFailFastOnSchemaDriftWhenDdlAutoValidateIsEnabled() {
        assertThatThrownBy(() -> new SpringApplicationBuilder(IdentityServiceApplication.class)
                        .web(WebApplicationType.NONE)
                        .profiles("test")
                        .run(
                                "--spring.datasource.url=" + DRIFT_POSTGRESQL_CONTAINER.getJdbcUrl(),
                                "--spring.datasource.username=" + DRIFT_POSTGRESQL_CONTAINER.getUsername(),
                                "--spring.datasource.password=" + DRIFT_POSTGRESQL_CONTAINER.getPassword(),
                                "--spring.datasource.driver-class-name=" + DRIFT_POSTGRESQL_CONTAINER.getDriverClassName(),
                                "--spring.flyway.url=" + DRIFT_POSTGRESQL_CONTAINER.getJdbcUrl(),
                                "--spring.flyway.user=" + DRIFT_POSTGRESQL_CONTAINER.getUsername(),
                                "--spring.flyway.password=" + DRIFT_POSTGRESQL_CONTAINER.getPassword(),
                                "--spring.flyway.locations=classpath:db/drift",
                                "--spring.jpa.hibernate.ddl-auto=validate")
                        .close())
                .hasStackTraceContaining("Schema-validation: missing table");
    }
}
