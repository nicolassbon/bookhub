package com.bookhub.catalog.config;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OpenLibraryProperties.class)
public class CatalogSearchConfig {

    @Bean
    public RestClient openLibraryRestClient(final OpenLibraryProperties openLibraryProperties) {
        return RestClient.builder()
                .baseUrl(openLibraryProperties.url())
                .build();
    }

    @Bean
    public Duration providerTimeout(final OpenLibraryProperties openLibraryProperties) {
        return Duration.ofMillis(openLibraryProperties.timeoutMs());
    }

    @Bean
    public Executor searchExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
