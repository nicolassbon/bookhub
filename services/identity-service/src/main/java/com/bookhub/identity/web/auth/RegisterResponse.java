package com.bookhub.identity.web.auth;

public record RegisterResponse(
        String userId,
        String username,
        String email,
        String displayName,
        String role) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String userId;
        private String username;
        private String email;
        private String displayName;
        private String role;

        private Builder() {
        }

        public Builder userId(final String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(final String username) {
            this.username = username;
            return this;
        }

        public Builder email(final String email) {
            this.email = email;
            return this;
        }

        public Builder displayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder role(final String role) {
            this.role = role;
            return this;
        }

        public RegisterResponse build() {
            return new RegisterResponse(userId, username, email, displayName, role);
        }
    }
}
