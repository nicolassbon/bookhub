package com.bookhub.identity.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class PostgreSqlIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRESQL_CONTAINER.start();
    }

    @Autowired
    private IdentityDatabaseCleaner identityDatabaseCleaner;

    @DynamicPropertySource
    static void configureDataSource(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRESQL_CONTAINER::getDriverClassName);
        registry.add("spring.flyway.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.flyway.password", POSTGRESQL_CONTAINER::getPassword);
    }

    @BeforeEach
    void cleanDatabase() {
        identityDatabaseCleaner.clean();
    }
}
