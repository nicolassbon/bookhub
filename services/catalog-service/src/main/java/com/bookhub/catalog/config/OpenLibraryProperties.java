package com.bookhub.catalog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "catalog.providers.openlibrary")
public record OpenLibraryProperties(
        boolean enabled,
        String url,
        int timeoutMs) {
}
