package com.bookhub.identity.web.error;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Instant timestamp;
        private int status;
        private String error;
        private String code;
        private String message;
        private String path;

        private Builder() {
        }

        public Builder timestamp(final Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder status(final int status) {
            this.status = status;
            return this;
        }

        public Builder error(final String error) {
            this.error = error;
            return this;
        }

        public Builder code(final String code) {
            this.code = code;
            return this;
        }

        public Builder message(final String message) {
            this.message = message;
            return this;
        }

        public Builder path(final String path) {
            this.path = path;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(timestamp, status, error, code, message, path);
        }
    }
}
