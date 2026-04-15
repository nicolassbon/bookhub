package com.bookhub.catalog.application.support;

import java.util.Locale;

public final class BookNormalization {

    private static final String WORKS_PREFIX = "/works/";

    private BookNormalization() {
    }

    public static String normalizeIsbn13(final String isbn13) {
        if (isbn13 == null || isbn13.isBlank()) {
            return null;
        }
        return isbn13
                .replace("-", "")
                .replace(" ", "")
                .trim();
    }

    public static String normalizeSourceReference(final String sourceReference) {
        if (sourceReference == null || sourceReference.isBlank()) {
            return null;
        }

        final String trimmed = sourceReference.trim();
        final String unprefixed = trimmed.startsWith(WORKS_PREFIX)
                ? trimmed.substring(WORKS_PREFIX.length())
                : trimmed;

        return unprefixed.toUpperCase(Locale.ROOT);
    }
}
