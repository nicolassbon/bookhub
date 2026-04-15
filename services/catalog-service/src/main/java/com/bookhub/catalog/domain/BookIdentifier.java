package com.bookhub.catalog.domain;

import com.bookhub.catalog.application.error.InvalidBookIdException;
import java.util.UUID;

public record BookIdentifier(String rawValue, Type type, UUID localId, String sourceReference) {

    private static final String EXTERNAL_PREFIX = "ext:ol:";

    public enum Type {
        LOCAL,
        EXTERNAL
    }

    public static BookIdentifier parse(final String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new InvalidBookIdException("Book ID must not be blank");
        }

        if (rawValue.startsWith(EXTERNAL_PREFIX)) {
            final String reference = rawValue.substring(EXTERNAL_PREFIX.length()).trim();
            if (reference.isEmpty()) {
                throw new InvalidBookIdException("External book ID is invalid");
            }
            return new BookIdentifier(rawValue, Type.EXTERNAL, null, reference);
        }

        try {
            return new BookIdentifier(rawValue, Type.LOCAL, UUID.fromString(rawValue), null);
        } catch (IllegalArgumentException exception) {
            throw new InvalidBookIdException("Book ID format is invalid");
        }
    }

    public boolean isLocal() {
        return type == Type.LOCAL;
    }

    public boolean isExternal() {
        return type == Type.EXTERNAL;
    }
}
